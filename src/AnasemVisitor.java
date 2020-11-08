import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Pair;
import java.util.Stack;

public class AnasemVisitor extends AnasintBaseVisitor<Integer> {
    public final Integer TIPO_NUM = 100;
    public final Integer TIPO_LOG = 101;

    HashMap<String,String> vars_global = new HashMap<String, String>();
    HashMap<String,String> vars_local = null;

    //HashMap<String,LinkedList<Pair<String,String>>> funciones_parametros = new HashMap<String, LinkedList<Pair<String,String>>>()>();
    HashMap<String,HashMap<String,String>> funciones_parametros = new HashMap<String, HashMap<String, String>>();
    HashMap<String,HashMap<String,String>> funciones_devuelve = new HashMap<String, HashMap<String, String>>();

    Stack<HashMap<String,String>> almacen_varibles = new Stack<HashMap<String, String>>();

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
            if(visit(ctx.expresion_condicional1())!=TIPO_LOG){
                return -1;
            }
        }
        return TIPO_LOG;
    }

    //Manejo de funciones

    @Override
    public Integer visitFuncion(Anasint.FuncionContext ctx) {
        vars_local = new HashMap<String, String>();

        visit(ctx.identificador_funcion());
        vars_local.putAll(funciones_parametros.get(ctx.identificador_funcion().IDENT().getText()));

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
    public Integer visitProcedimiento(Anasint.ProcedimientoContext ctx) {
        vars_local = new HashMap<String, String>();

        visit(ctx.identificador_procedimiento());
        vars_local.putAll(funciones_parametros.get(ctx.identificador_procedimiento().IDENT().getText()));

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

        if(funciones_parametros.containsKey(nombre_subprograma)){
            System.out.println("Subprograma "+nombre_subprograma+" definido previamente!");
            return -1;
        }

        HashMap<String,String> parametros_in = new HashMap<String, String>();
        for(Anasint.Argumento_subprogramaContext arg : ctx.argumento_subprograma()){
            if(parametros_in.containsKey(arg.IDENT().getText())) {
                System.err.println("Variable ya definida!");
                return -1;
            }else if(vars_global.containsKey(arg.IDENT().getText())){
                System.err.println("Variable ya definida globalmente!");
                return -1;
            }else {
                parametros_in.put(arg.IDENT().getText(), arg.tipo_de_dato().getText());
            }
        }
        funciones_parametros.put(nombre_subprograma,parametros_in);

        HashMap<String,String> parametros_out = new HashMap<String, String>();
        for(Anasint.Argumento_subprograma_devContext arg : ctx.argumento_subprograma_dev()){
            if(parametros_out.containsKey(arg.IDENT().getText())){
                System.err.println("Variable "+arg.IDENT().getText()+" repetida!");
                return -1;
            }else if(vars_global.containsKey(arg.IDENT().getText())){
                System.err.println("Variable "+arg.IDENT().getText()+" definida globalmente!");
                return -1;
            } else {
                parametros_out.put(arg.IDENT().getText(), arg.tipo_de_dato().getText());
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

        HashMap<String,String> parametros_in = new HashMap<String, String>();
        for(Anasint.Argumento_subprogramaContext arg : ctx.argumento_subprograma()){
            if(parametros_in.containsKey(arg.IDENT().getText())){
                System.err.println("Variable "+arg.IDENT().getText()+" repetida!");
                return -1;
            }else if(vars_global.containsKey(arg.IDENT().getText())){
                System.err.println("Variable "+arg.IDENT().getText()+" definida globalmente!");
                return -1;
            }else {
                parametros_in.put(arg.IDENT().getText(), arg.tipo_de_dato().getText());
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
            }else{
                System.err.println("La variable "+ctx.IDENT().getText()+" no debe ser una lista!");
            }
        }else{
            System.err.println("Variable "+ctx.IDENT().getText()+" no declarada!");
        }
        return -1;
    }

    @Override
    public Integer visitAsignFunc(Anasint.AsignFuncContext ctx) {
        if(!funciones_parametros.containsKey(ctx.IDENT().getText())){
            System.err.println("La funcion "+ctx.IDENT().getText()+" no esta definida");
            return -1;
        }

        if(ctx.expresion_asignacion().size() != funciones_parametros.get(ctx.IDENT().getText()).size()){
            System.out.println("Se espereban "+funciones_parametros.get(ctx.IDENT().getText()).size()+" recibidos"+
                    ctx.expresion_asignacion().size());
            return -1;
        }

        /**for(int i = 0;i < ctx.expresion_asignacion().size();i++){
            if(visit(ctx.expresion_asignacion(i)) != funciones_parametros.get(ctx.IDENT().getText()).

        }**/
        return -1;
    }

    @Override
    public Integer visitAsigParentesis(Anasint.AsigParentesisContext ctx) {
        return visit(ctx.expresion_asignacion());
    }

    //Tipos de instrucciones

    @Override
    public Integer visitIns_asignacion(Anasint.Ins_asignacionContext ctx) {
        //a,c = 1,2,9

        if(ctx.identificador_variables().size() != ctx.expresion_asignacion().size()){
            System.err.println("Asginacion mal formulada!");
        }

        for(int i = 0;i < ctx.identificador_variables().size();i++){
            if(visit(ctx.identificador_variables(i)) != visit(ctx.expresion_asignacion(i))){
                System.err.println("Asignacion mal tipada");
            }
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
            }else if(vars_global.get(ctx.IDENT().getText()).equals("LOG")){
                return TIPO_LOG;
            }else{
                System.out.println("La variable "+ctx.IDENT().getText()+" no puede ser una lista");
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