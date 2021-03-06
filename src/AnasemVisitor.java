import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AnasemVisitor extends AnasintBaseVisitor<Integer> {

    public final Integer TIPO_NUM =     100;
    public final Integer TIPO_NUM_SEQ = 101;
    public final Integer TIPO_LOG =     102;
    public final Integer TIPO_LOG_SEQ = 103;
    public final Integer DEV_MULTIPLE = 104;
    public final Integer DEV_NADA = 105;

    private class Parametro {
        String nombre;
        String tipo;

        public String toString(){
            return nombre + ":" + tipo;
        }
    }

    HashMap<String,String> vars_global = new HashMap<String, String>();
    HashMap<String,String> vars_local = null;

    //Necesito una lista que mantenga el orden porque luego tengo que comprobar en que posicion esta
    HashMap<String,LinkedList<Parametro>> funciones_parametros = new HashMap<String, LinkedList<Parametro>>();
    HashMap<String,LinkedList<Parametro>> funciones_devuelve = new HashMap<String, LinkedList<Parametro>>();

    String subprograma_actual;
    Integer numero_devs;
    Boolean en_iteracion;

    @Override
    public Integer visitPrograma(Anasint.ProgramaContext ctx){
        visit(ctx.variables());
        //System.out.println(vars_global.toString());
        visit(ctx.subprogramas());
        //System.out.println(funciones_parametros.toString());
        //System.out.println(funciones_devuelve.toString());

        visit(ctx.instrucciones());
        return 0;
    }

    public Integer visitDecl_var(Anasint.Decl_varContext ctx) {

        //Decision de diseño 2
        for(String i : ctx.identificador_declaracion().getText().split(",")) {
            if (vars_global.containsKey(i)){
                System.err.println("Variable "+i+" declarada previamente como "+vars_global.get(i));
                continue;
            }else {
                vars_global.put(i, ctx.tipo_de_dato().getText());
            }
        }
        return 0;
    }

    public Integer visitDecl_var(Anasint.Decl_varContext ctx,HashMap<String,String> map_vars) {

        //Decision de diseño 2
        for(String i : ctx.identificador_declaracion().getText().split(",")) {
            if (map_vars.containsKey(i)){
                System.err.println("Variable "+i+" declarada previamente como "+map_vars.get(i));
            }else if(vars_global.containsKey(i)){
                System.err.println("Variable "+i+" ya definida globalmente!");
                return -1;
            }else {
                map_vars.put(i, ctx.tipo_de_dato().getText());
            }
        }
        return 0;
    }

    @Override
    public Integer visitExpresion_asignacion(Anasint.Expresion_asignacionContext ctx) {
        if(ctx.operadores_aritmeticos() != null){
            //Asignacion Binaria
            return TIPO_NUM;
        }else{
            //Asignacion Unaria
            return visit(ctx.expresion_asignacion1());
        }
    }

    @Override
    public Integer visitExpresion_condicional(Anasint.Expresion_condicionalContext ctx) {
        if(ctx.operadores_binarios() != null){
            //Asignacion numerica
            if(ctx.operadores_binarios().MAYOR() != null
                    || ctx.operadores_binarios().MENOR() != null
                    || ctx.operadores_binarios().MAYORIGUAL() != null
                    || ctx.operadores_binarios().MENORIGUAL() != null){

                if(!(visit(ctx.expresion_condicional1())==TIPO_NUM
                        && visit(ctx.expresion_condicional())==TIPO_NUM)) {
                    System.err.println("La expresion aritmetica debe ser con variables tipo NUM!");
                    return -1;
                }
            }

            if(ctx.operadores_binarios().CONJUNCION() != null
                    || ctx.operadores_binarios().DISYUNCION() != null){

                if(!(visit(ctx.expresion_condicional1())==TIPO_LOG
                        && visit(ctx.expresion_condicional())==TIPO_LOG)) {
                    System.err.println("La expresion logica debe ser con expresiones logicas");
                    return -1;
                }
            }

            if(ctx.operadores_binarios().IGUAL() != null ||
                    ctx.operadores_binarios().DESIGUAL() != null){

                //Vemos que los dos operando son del mismo tipo
                if(visit(ctx.expresion_condicional1())!=visit(ctx.expresion_condicional())){
                    System.err.println("La igualdad debe ser entre operadores del mismo tipo");
                    return -1;
                }
            }
        }else{
            //Asignacion Unaria
            return visit(ctx.expresion_condicional1());
        }
        return TIPO_LOG;
    }

    //Manejo de funciones

    @Override
    public Integer visitFuncion(Anasint.FuncionContext ctx) {
        vars_local = new HashMap<String, String>();
        numero_devs = 0;

        visit(ctx.identificador_funcion());

        for(Anasint.Decl_varContext arg : ctx.variables().decl_var()) {
            visitDecl_var(arg,vars_local);
        }

        vars_global.putAll(vars_local);

        visit(ctx.instrucciones());

        for(String k : vars_local.keySet()){
            vars_global.remove(k);
        }

        if(numero_devs == 0){
            System.err.println("Deberia haber un dev en la función!");
        }

        vars_local = null;
        subprograma_actual = null;
        numero_devs = 0;

        return 0;
    }

    @Override
    public Integer visitProcedimiento(Anasint.ProcedimientoContext ctx) {
        vars_local = new HashMap<String, String>();

        visit(ctx.identificador_procedimiento());

        for(Anasint.Decl_varContext arg : ctx.variables().decl_var()) {
            visitDecl_var(arg,vars_local);
        }

        vars_global.putAll(vars_local);

        visit(ctx.instrucciones());

        for(String k : vars_local.keySet()){
            vars_global.remove(k);
        }

        vars_local = null;

        return 0;
    }

    @Override
    public Integer visitIdentificador_funcion(Anasint.Identificador_funcionContext ctx) {
        String nombre_subprograma = ctx.IDENT().getText();
        subprograma_actual = nombre_subprograma;

        if(funciones_parametros.containsKey(nombre_subprograma)){
            System.out.println("Subprograma "+nombre_subprograma+" definido previamente!");
            return -1;
        }

        LinkedList<Parametro> parametros_in = new LinkedList<Parametro>();
        for(Anasint.Argumento_subprogramaContext arg : ctx.argumento_subprograma()){
            Parametro aux = new Parametro();
            aux.nombre = arg.IDENT().getText();
            aux.tipo = arg.tipo_de_dato().getText();

            //Vemos que no esta la variable ni en las variables locales ni globales
            //Tambien aprovechamos para añadirlas a las variables locales
            if(parametros_in.stream().anyMatch(x -> x.nombre.equals(aux.nombre))) {
                System.err.println("Variable ya definida!");
                return -1;
            }else if(vars_global.containsKey(aux.nombre)){
                System.err.println("Variable ya definida globalmente!");
                return -1;
            }else {
                parametros_in.add(aux);
                vars_local.put(aux.nombre,aux.tipo);
            }
        }
        funciones_parametros.put(nombre_subprograma,parametros_in);

        LinkedList<Parametro> parametros_out = new LinkedList<Parametro>();
        for(Anasint.Argumento_subprograma_devContext arg : ctx.argumento_subprograma_dev()){
            Parametro aux = new Parametro();
            aux.nombre = arg.IDENT().getText();
            aux.tipo = arg.tipo_de_dato().getText();

            //Vemos que no esta la variable ni en las variables locales ni globales
            if(parametros_out.stream().anyMatch(x -> x.nombre.equals(aux.nombre))) {
                System.err.println("Variable ya definida!");
                return -1;
            }else if(vars_global.containsKey(aux.nombre)){
                System.err.println("Variable ya definida globalmente!");
                return -1;
            }else {
                parametros_out.add(aux);
                vars_local.put(aux.nombre,aux.tipo);
            }
        }
        funciones_devuelve.put(nombre_subprograma,parametros_out);

        return 0;
    }

    @Override
    public Integer visitIdentificador_procedimiento(Anasint.Identificador_procedimientoContext ctx) {
        String nombre_subprograma = ctx.IDENT().getText();

        if(funciones_parametros.containsKey(nombre_subprograma)){
            System.out.println("Subprograma "+nombre_subprograma+" definido previamente!");
            return -1;
        }

        LinkedList<Parametro> parametros_in = new LinkedList<Parametro>();
        for(Anasint.Argumento_subprogramaContext arg : ctx.argumento_subprograma()){
            Parametro aux = new Parametro();
            aux.nombre = arg.IDENT().getText();
            aux.tipo = arg.tipo_de_dato().getText();

            //Vemos que no esta la variable ni en las variables locales ni globales
            //Tambien aprovechamos para añadirlas a las variables locales
            if(parametros_in.stream().anyMatch(x -> x.nombre.equals(aux.nombre))) {
                System.err.println("Variable ya definida!");
                return -1;
            }else if(vars_global.containsKey(aux.nombre)){
                System.err.println("Variable ya definida globalmente!");
                return -1;
            }else {
                parametros_in.add(aux);
                vars_local.put(aux.nombre,aux.tipo);
            }
        }
        funciones_parametros.put(nombre_subprograma,parametros_in);

        return 0;
    }

    //Tipos de condicionales
    public Integer visitCondNegacion(Anasint.CondNegacionContext ctx) {
        if(visit(ctx.expresion_condicional()) != TIPO_LOG){
            System.err.println("La negación debe ser con variables tipo LOG!");
            return -1;
        }
        return TIPO_LOG;
    }

    public Integer visitCondParentesis(Anasint.CondParentesisContext ctx) {
        return visitExpresion_condicional(ctx.expresion_condicional());
    }

    @Override
    public Integer visitCondCierto(Anasint.CondCiertoContext ctx) {
        return TIPO_LOG;
    }

    @Override
    public Integer visitCondFalse(Anasint.CondFalseContext ctx) {
        return TIPO_LOG;
    }

    @Override
    public Integer visitCondVar(Anasint.CondVarContext ctx) {
        return visit(ctx.expresion_asignacion());
    }

    //Tipos de asignacion
    @Override
    public Integer visitAsigFalse(Anasint.AsigFalseContext ctx) {
        return TIPO_LOG;
    }

    @Override
    public Integer visitAsigTrue(Anasint.AsigTrueContext ctx) {
        return TIPO_LOG;
    }

    @Override
    public Integer visitAsigLista(Anasint.AsigListaContext ctx) {
        if(visit(ctx.expresion_asignacion()) != TIPO_NUM){
            System.err.println("El indice la lista debe ser un numero!");
            return -1;
        }

        if(vars_global.containsKey(ctx.IDENT().getText())){
            if(vars_global.get(ctx.IDENT().getText()).equals("SEQ(LOG)")) {
                return TIPO_LOG;
            }else if(vars_global.get(ctx.IDENT().getText()).equals("SEQ(NUM)")) {
                return TIPO_NUM;
            }else{
                System.err.println("La variable "+ctx.IDENT().getText()+" no es una lista!");
            }
        }else{
            System.err.println("Variable "+ctx.IDENT().getText()+" no declarada!");
        }

        return -1;
    }

    @Override
    public Integer visitAsigExplicit(Anasint.AsigExplicitContext ctx) {
        return TIPO_NUM;
    }

    @Override
    public Integer visitAsigSimple(Anasint.AsigSimpleContext ctx) {
        if(vars_global.containsKey(ctx.IDENT().getText())){
            if(vars_global.get(ctx.IDENT().getText()).equals("LOG")) {
                return TIPO_LOG;
            }else if(vars_global.get(ctx.IDENT().getText()).equals("NUM")) {
                return TIPO_NUM;
            }else if(vars_global.get(ctx.IDENT().getText()).equals("SEQ(NUM)")) {
                return TIPO_NUM_SEQ;
            }else if(vars_global.get(ctx.IDENT().getText()).equals("SEQ(LOG)")) {
                return TIPO_LOG_SEQ;
            }

        }else{
            System.err.println("Variable "+ctx.IDENT().getText()+" no declarada!");
        }
        return -1;
    }

    @Override
    public Integer visitAsignFunc(Anasint.AsignFuncContext ctx) {
        //Comprobamos que la funcion esta definida
        if(!funciones_parametros.containsKey(ctx.IDENT().getText())){
            System.err.println("La funcion "+ctx.IDENT().getText()+" no esta definida");
            return -1;
        }

        //Comprobamos que el numero de variables coincide
        if(ctx.expresion_asignacion().size() != funciones_parametros.get(ctx.IDENT().getText()).size()) {
            System.err.println("Se espereban " + funciones_parametros.get(ctx.IDENT().getText()).size() + " parametro(s) recibido(s) " +
                    ctx.expresion_asignacion().size() + " parametro(s)");

        }else{
            LinkedList<Parametro> params = funciones_parametros.get(ctx.IDENT().getText());

            //Comprobamos que el tipo de las variables coincide
            for (int i = 0; i < ctx.expresion_asignacion().size(); i++) {
                if (visit(ctx.expresion_asignacion(i)) == TIPO_NUM && !params.get(i).tipo.equals("NUM")) {
                    System.err.println("Parametros incorrectos al llamar al subprogama!");
                } else if (visit(ctx.expresion_asignacion(i)) == TIPO_LOG && !params.get(i).tipo.equals("LOG")) {
                    System.err.println("Parametros incorrectos al llamar al subprogama!");
                } else if (visit(ctx.expresion_asignacion(i)) == TIPO_LOG_SEQ && !params.get(i).tipo.equals("SEQ(LOG)")) {
                    System.err.println("Parametros incorrectos al llamar al subprogama!");
                } else if (visit(ctx.expresion_asignacion(i)) == TIPO_NUM_SEQ && !params.get(i).tipo.equals("SEQ(NUM)")) {
                    System.err.println("Parametros incorrectos al llamar al subprogama!");
                }
            }
        }

        //Vemos que devuelve la funcion
        LinkedList<Parametro> params_dev = funciones_devuelve.get(ctx.IDENT().getText());
        if(params_dev == null) {
            return DEV_NADA;
        }else if(params_dev.size() == 0){
            return  DEV_NADA;
        }else if(params_dev.size() == 1){
            if(params_dev.get(0).tipo.equals("NUM")) return TIPO_NUM;
            if(params_dev.get(0).tipo.equals("LOG")) return TIPO_LOG;
            if(params_dev.get(0).tipo.equals("SEQ(NUM)")) return TIPO_NUM_SEQ;
            if(params_dev.get(0).tipo.equals("SEQ(LOG)")) return TIPO_LOG_SEQ;
        }else{
            return DEV_MULTIPLE;
        }

        return -1;
    }

    @Override
    public Integer visitAsigParentesis(Anasint.AsigParentesisContext ctx) {
        return visit(ctx.expresion_asignacion());
    }

    @Override
    public Integer visitAsigExplicitLista(Anasint.AsigExplicitListaContext ctx){
        if(ctx.expresion_asignacion().size() == 0){
            System.err.println("Asignacion no tipada");
            return -1;
        }else if(ctx.expresion_asignacion().size() == 1){
            return visit(ctx.expresion_asignacion(0));
        }else{
            //Comprobamos que todos los elementos de la lista son iguales
            Integer list_tipo = visit(ctx.expresion_asignacion(0));
            if(ctx.expresion_asignacion().stream().mapToInt(x -> visit(x)).allMatch(x -> x == list_tipo)){
                if(list_tipo == TIPO_LOG){
                    return TIPO_LOG_SEQ;
                }else if(list_tipo == TIPO_NUM){
                    return TIPO_NUM_SEQ;
                }
            }
        }
        return 0;
    }

    //Tipos de instrucciones

    @Override
    public Integer visitIns_asignacion(Anasint.Ins_asignacionContext ctx) {
        //a = 10;
        //a,c = 1,2,9;
        //a,c = func2_devuelve2valores();

        //Solucion: tener una variable por cada operando
        // ids-+      vals-+
        //     v           v
        // a,c,d = func2(),1,2;

        Integer ids = 0,vals = 0;
        List<Anasint.Identificador_variablesContext> idents = ctx.identificador_variables();
        List<Anasint.Expresion_asignacionContext> valores = ctx.expresion_asignacion();

        while(ids < idents.size() && vals < valores.size()){
            Integer valor_actual = visit(valores.get(vals));
            Integer ident_actual = visit(idents.get(ids));
            if(valor_actual == DEV_NADA){
                System.err.println("Asginacion sin valor!");
                return -1;
            }else if(valor_actual == DEV_MULTIPLE){
                //Aqui podemos estar seguro que llamamos a un subprograma
                //Vamos a obtener su identificador para saber cuantos valores devuelve
                Anasint.Expresion_asignacion1Context nombreSuprogamaCtx = valores.get(vals).expresion_asignacion1();
                String nombreSubprograma = nombreSuprogamaCtx.children.get(0).getText();

                for( Parametro p :funciones_devuelve.get(nombreSubprograma)){
                    ident_actual = visit(idents.get(ids));
                    if(p.tipo.equals("NUM") && ident_actual != TIPO_NUM){
                        System.err.println("Asginacion mal tipada");
                        return -1;
                    } else if(p.tipo.equals("LOG") && ident_actual != TIPO_LOG){
                        System.err.println("Asginacion mal tipada");
                        return -1;
                    } else if(p.tipo.equals("SEQ(NUM)") && ident_actual != TIPO_NUM_SEQ){
                        System.err.println("Asginacion mal tipada");
                        return -1;
                    } else if(p.tipo.equals("SEQ(LOG)") && ident_actual != TIPO_LOG_SEQ){
                        System.err.println("Asginacion mal tipada");
                        return -1;
                    }

                    ids++;
                }

                vals++;
            }else{
                if(ident_actual != valor_actual){
                    System.err.println("Asginacion mal tipada");
                    return -1;
                }
                vals++;
                ids++;
            }
        }

        return 0;
    }

    @Override
    public Integer visitIns_devolucion(Anasint.Ins_devolucionContext ctx) {
        numero_devs++;

        if(subprograma_actual == null){
            System.err.println("No se puede usar la instruccion dev fuera de una funcion!");
            return -1;
        }else{
            List<Parametro> vals = funciones_devuelve.get(subprograma_actual);
            if(vals.size() != ctx.expresion_asignacion().size()){
                System.err.println("Se espereban " + vals.size() + " parametro(s) recibido(s) " +
                        ctx.expresion_asignacion().size() + " parametro(s)");
                return -1;
            }

            //Comprobamos que el tipo de las variables coincide
            for (int i = 0; i < vals.size(); i++) {
                if (visit(ctx.expresion_asignacion(i)) == TIPO_NUM && !vals.get(i).tipo.equals("NUM")) {
                    System.err.println("La funcion no devuelve esos valores!");
                } else if (visit(ctx.expresion_asignacion(i)) == TIPO_LOG && !vals.get(i).tipo.equals("LOG")) {
                    System.err.println("La funcion no devuelve esos valores!");
                } else if (visit(ctx.expresion_asignacion(i)) == TIPO_LOG_SEQ && !vals.get(i).tipo.equals("SEQ(LOG)")) {
                    System.err.println("La funcion no devuelve esos valores!");
                } else if (visit(ctx.expresion_asignacion(i)) == TIPO_NUM_SEQ && !vals.get(i).tipo.equals("SEQ(NUM)")) {
                    System.err.println("La funcion no devuelve esos valores!");
                }
            }
        }

        return 0;
    }

    @Override
    public Integer visitIns_iteracion(Anasint.Ins_iteracionContext ctx) {
        en_iteracion = true;
        super.visitIns_iteracion(ctx);
        en_iteracion = false;
        return 0;
    }

    @Override
    public Integer visitIns_ruptura(Anasint.Ins_rupturaContext ctx) {
        if(!en_iteracion){
            System.err.println("No se puede hacer una ruptura fuera de un bucle!");
        }
        return 0;
    }


    //Identificador variables

    @Override
    public Integer visitIdentVarSimple(Anasint.IdentVarSimpleContext ctx) {
        if(!vars_global.containsKey(ctx.IDENT().getText())){
            System.err.println("Variable "+ctx.IDENT().getText()+" no declarada");
        }else{
            if(vars_global.get(ctx.IDENT().getText()).equals("NUM")){
                return TIPO_NUM;
            }else if(vars_global.get(ctx.IDENT().getText()).equals("LOG")) {
                return TIPO_LOG;
            }else if(vars_global.get(ctx.IDENT().getText()).equals("SEQ(NUM)")){
                return TIPO_NUM_SEQ;
            }else if(vars_global.get(ctx.IDENT().getText()).equals("SEQ(LOG)")){
                return TIPO_LOG_SEQ;
            }
        }

        return -1;
    }

    @Override
    public Integer visitIdentLista(Anasint.IdentListaContext ctx) {
        if(visit(ctx.expresion_asignacion()) != TIPO_NUM){
            System.err.println("El indice la lista debe ser un numero!");
            return -1;
        }

        if(!vars_global.containsKey(ctx.IDENT().getText())){
            System.err.println("Variable "+ctx.IDENT().getText()+" no declarada");
        }else{
            if(vars_global.get(ctx.IDENT().getText()).equals("SEQ(NUM)")){
                return TIPO_NUM;
            }else if(vars_global.get(ctx.IDENT().getText()).equals("SEQ(LOG)")){
                return TIPO_LOG;
            }else{
                System.out.println("La variable "+ctx.IDENT().getText()+" no puede ser una lista");
            }
        }

        return -1;
    }
}