import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Interprete extends AnasintBaseVisitor<String>{

    //ALMACEN DE VARIABLES
    List<List<Tupla>> vars_globales = new ArrayList<>();

    //ALMACEN DE FUNCIONES
    HashMap<String,Anasint.FuncionContext> funcion_globales = new HashMap<String, Anasint.FuncionContext>();

    //FUNCIONA
    //VisitPrograma
    public String visitPrograma(Anasint.ProgramaContext ctx){
        vars_globales.add(new ArrayList<Tupla>());
        visit(ctx.variables());
        visit(ctx.subprogramas());
        visit(ctx.instrucciones());
        return "";
    }

    //FUNCIONA
    public String visitDecl_var(Anasint.Decl_varContext ctx){
        String ident;
        String tipo;
        for(String i : ctx.identificador_declaracion().getText().split(",")) {
            ident = i;
            tipo = ctx.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
            System.out.println(vars_globales);
        }
        return "";
    }

    public String visitSubprogramas(Anasint.SubprogramasContext ctx){
        for(Anasint.FuncionContext sp : ctx.funcion()){
            funcion_globales.put(sp.identificador_funcion().IDENT().getText(), sp);
        }

        return "";
    }

    /*FUNCIONES CUANDO SON DECLARADAS
    //FUNCIONA
    public String visitFuncion(Anasint.FuncionContext ctx){
        //Decision 1.2
        vars_globales.add(new ArrayList<Tupla>());
        visitIdentificador_funcion(ctx.identificador_funcion());
        visit(ctx.variables());

        return visitInstrucciones(ctx.instrucciones());
    }

    //FUNCIONA
    public String visitIdentificador_funcion(Anasint.Identificador_funcionContext ctx){
        String ident;
        String tipo;
        for(Anasint.Argumento_subprogramaContext i : ctx.argumento_subprograma()) {
            ident = i.IDENT().getText();
            tipo = i.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
            System.out.println(vars_globales);
        }

        return "";
    }*/
    //FUNCIONES CUANDO SON LLAMADAS
    public String visitFuncion(Anasint.FuncionContext ctx, List<String> valores){
        //Decision 1.2
        vars_globales.add(new ArrayList<Tupla>());
        visitIdentificador_funcion(ctx.identificador_funcion(),valores);
        visit(ctx.variables());

        return visitInstrucciones(ctx.instrucciones());
    }
    public String visitIdentificador_funcion(Anasint.Identificador_funcionContext ctx, List<String> valores){
        String ident;
        String tipo;
        for(Anasint.Argumento_subprogramaContext i : ctx.argumento_subprograma()) {
            ident = i.IDENT().getText();
            tipo = i.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
        }
        for(int i = 0; i < ctx.argumento_subprograma().size(); i++) {
            ident = ctx.argumento_subprograma().get(i).IDENT().getText();
            actualizar_valor(ident, valores.get(i));
            System.out.println(vars_globales);
        }
        return "";
    }

    //FUNCIONA
    public String visitProcedimiento(Anasint.ProcedimientoContext ctx){
        //Decision 1.2
        vars_globales.add(new ArrayList<Tupla>());
        visitIdentificador_procedimiento(ctx.identificador_procedimiento());
        visit(ctx.variables());
        visit(ctx.instrucciones());
        return "";
    }

    //FUNCIONA
    public String visitIdentificador_procedimiento(Anasint.Identificador_procedimientoContext ctx){
        String ident;
        String tipo;
        for(Anasint.Argumento_subprogramaContext i : ctx.argumento_subprograma()) {
            ident = i.IDENT().getText();
            tipo = i.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
            System.out.println(vars_globales);
        }

        return "";
    }
    //INSTRUCCIONES

    //FUNCIONA
    public String visitInstrucciones(Anasint.InstruccionesContext ctx){

        String res = "";

        for (int i = 0; i < ctx.tipo_instruccion().size(); i++){
            System.out.println(ctx.tipo_instruccion(i).getText());
            res = visit(ctx.tipo_instruccion(i));

        }
        return res;
    }

    //FUNCIONA
    public String visitIns_asignacion(Anasint.Ins_asignacionContext ctx){

        List<Anasint.Identificador_variablesContext> idents = ctx.identificador_variables();
        List<Anasint.Expresion_asignacionContext> valores = ctx.expresion_asignacion();

        List<String> ids = new ArrayList<String>();
        List<String> vals = new ArrayList<String>();

        for(Anasint.Identificador_variablesContext i : idents){
            visit(i);
            ids.add(i.getText());
        }
        for(Anasint.Expresion_asignacionContext e : valores){
            String esxp = visitExpr_asig(e);
            vals.add(esxp);
        }

        for(int i = 0; i < ids.size(); i++){
            actualizar_valor(ids.get(i), vals.get(i));
        }

        System.out.println(vars_globales);
        return "";
    }

    //FUNCIONA
    public String visitExpr_asig(Anasint.Expresion_asignacionContext ctx){

        String res = "";
        if(ctx.operadores_aritmeticos()!=null){
            Integer n = Integer.parseInt(ctx.expresion_asignacion1().getText());
            switch (ctx.operadores_aritmeticos().getText()){
                case "+":
                    n += Integer.parseInt(visitExpr_asig(ctx.expresion_asignacion()));
                    break;
                case "-":
                    n -= Integer.parseInt(visitExpr_asig(ctx.expresion_asignacion()));
                    break;
                case "*":
                    n *= Integer.parseInt(visitExpr_asig(ctx.expresion_asignacion()));
                default:
                    break;
            }
            res = n.toString();
        }else{
            res = visit(ctx.expresion_asignacion1());
        }

        return res;
    }
    //TERMINADO
    public String visitAsigTrue(Anasint.AsigTrueContext ctx) {
        return "T";
    }
    //TERMINADO
    public String visitAsigFalse(Anasint.AsigFalseContext ctx) {
        return "F";
    }
    //TERMINADO
    public String visitAsigExplicit(Anasint.AsigExplicitContext ctx) {
        return ctx.getText();
    }
    //TERMINADO
    public String visitAsigLista(Anasint.AsigListaContext ctx) {
        return devolver_valor(ctx.getText());
    }
    //TERMINADO
    public String visitAsignFunc(Anasint.AsignFuncContext ctx) {
        Anasint.FuncionContext funcion = funcion_globales.get(ctx.IDENT().getText());
        List<String> valores = new ArrayList<>();
        for(Anasint.Expresion_asignacionContext exp : ctx.expresion_asignacion()){
            valores.add(visit(exp));
        }
        return visitFuncion(funcion,valores);
    }
    //TERMINADO
    public String visitAsigParentesis(Anasint.AsigParentesisContext ctx) {
        return visit(ctx.expresion_asignacion());
    }
    //TERMINADO
    public String visitAsigExplicitLista(Anasint.AsigExplicitListaContext ctx) {
        String res = "[";
        for(Anasint.Expresion_asignacionContext exp : ctx.expresion_asignacion()){
            res += visit(exp) +",";
        }
        res = res.substring(0,res.length()-1)+"]";
        return res;
    }
    //TERMINADO
    public String visitAsigSimple(Anasint.AsigSimpleContext ctx) {
        return devolver_valor(ctx.getText());
    }

    //FUNCIONA
    public String visitIns_condicion(Anasint.Ins_condicionContext ctx){

        Boolean condicion = visitExpr_cond(ctx.expresion_condicional());
        List<Anasint.Tipo_instruccionContext> ins = ctx.tipo_instruccion();
        List<Anasint.Tipo_instruccion2Context> ins2 = ctx.tipo_instruccion2();

        if(condicion){
            for (int i = 0; i<ins.size();i++){
                visitTipo_instruccion(ctx.tipo_instruccion(i));
            }
        }else{
            for (int i = 0; i<ins2.size();i++){
                visitTipo_instruccion(ctx.tipo_instruccion2(i).tipo_instruccion());
            }
        }

        return "";
    }

    //FUNCIONA
    public Boolean visitExpr_cond(Anasint.Expresion_condicionalContext ctx){

        Boolean condicion = false;
        String s = visitExpr_cond1(ctx.expresion_condicional1());
        if(es_un_ident(s)){
            s = devolver_valor(s);
        }

        if(ctx.operadores_binarios() != null){
            Boolean condicionD = visitExpr_cond(ctx.expresion_condicional());
            String s2 = visitExpr_cond1(ctx.expresion_condicional().expresion_condicional1());

            if(es_un_ident(s2)){
                s2 = devolver_valor(s2);
            }

            switch (ctx.operadores_binarios().getText()){
                case "&&":
                    condicion = (Boolean.parseBoolean(s)) && condicionD;
                    break;
                case "||":
                    condicion = (Boolean.parseBoolean(s)) || condicionD;
                    break;
                case "==":
                    condicion = (s == s2);
                    break;
                case "!=":
                    condicion = (s != s2);
                    break;
                case ">":
                    condicion = (Integer.parseInt(s) > Integer.parseInt(s2));
                    break;
                case "<":
                    condicion = (Integer.parseInt(s) < Integer.parseInt(s2));
                    break;
                case ">=":
                    condicion = (Integer.parseInt(s) >= Integer.parseInt(s2));
                    break;
                case "<=":
                    condicion = (Integer.parseInt(s) <= Integer.parseInt(s2));
                    break;
                default:
                    break;
            }
        }else {
            condicion = Boolean.parseBoolean(s);
        }

        return condicion;
    }

    //COMPLETAR
    public String visitExpr_cond1(Anasint.Expresion_condicional1Context ctx){

        String res = "";
        String c = ctx.getText();

        if(c.equals("cierto") || c.equals("falso")){
            res = c;
        }else if(c.startsWith("!")){

        }else if(c.startsWith("(")){

        }else{
            res = c;
        }

        return res;
    }

    //FUNCIONA
    public Integer visitIns_itera(Anasint.Ins_iteracionContext ctx){

        Boolean condicion = visitExpr_cond(ctx.expresion_condicional());
        List<Anasint.Tipo_instruccionContext> ins = ctx.tipo_instruccion();

        if(condicion){
            for(int i = 0; i<ins.size(); i++) {
                if(ctx.tipo_instruccion(i).equals(Anasint.RUPTURA)){
                    return 0;
                }
                visit(ctx.tipo_instruccion(i));
            }
        }

        return 0;
    }

    //COMPROBAR
    public String visitIns_devolucion(Anasint.Ins_devolucionContext ctx){

        List<Anasint.Expresion_asignacionContext> as = ctx.expresion_asignacion();
        String res = "";
        for(int i = 0; i<as.size(); i++){
            res += visitExpr_asig(ctx.expresion_asignacion(i)) + ",";
        }
        res = res.substring(0,res.length()-1);
        eliminaVarsLocales();

        return res;
    }

    //FUNCIONA
    public String visitIns_mostrar(Anasint.Ins_mostrarContext ctx){

        List<Anasint.Expresion_asignacionContext> as = ctx.expresion_asignacion();
        String res = "Mostrando: ";
        for(int i = 0; i<as.size(); i++){
            if(existe(as.get(i).getText())) {
                res += devolver_valor(as.get(i).getText())+ ", ";
            }
            else{
                res += visitExpr_asig(as.get(i))+ ", ";
            }
        }
        res = res.substring(0,res.length()-2);
        System.out.println(res);
        return "";
    }

    //FUNCIONES UTILES

    //GUARDAR UNA VARIABLE AL DECLARARLA
    public void declarar_variable(String ident, String tipo){
        String valor;
        if(tipo.equals("NUM")) valor = "0";
        else if(tipo.equals("LOG")) valor = "T";
        else valor = "[]";
        vars_globales.get(vars_globales.size()-1).add(new Tupla(ident,valor));
    }

    //ACTUALIZAR UNA VARIABLE CON UN NUEVO VALOR
    public void actualizar_valor(String ident, String valor){
        for (List<Tupla> ls: vars_globales){
            for(Tupla tpl : ls) {
                if (ident.equals(tpl.getV1())) tpl.putV2(valor);
            }
        }
    }

    //EXISTE LA VARIABLE?
    public Boolean existe(String ident){
        for (List<Tupla> ls : vars_globales) {
            for (Tupla tpl : ls) {
                if (ident.equals(tpl.getV1())) return true;
            }
        }
        return false;
    }

    //ES UN IDENT
    Boolean es_un_ident(String ident){
        if(Character.isLetter(ident.charAt(0))){
            return existe(ident);
        }
        return false;
    }

    //BUSCA Y DEVUELVE EL VALOR DE UNA VARIABLE
    public String devolver_valor(String ident) {
        if(ident.endsWith("]")){
            String aux1 = ident.replace("]", "");
            String aux2 = aux1.replace("[", ";");
            String[] aux3 = aux2.split(";");
            for (List<Tupla> ls : vars_globales) {
                for (Tupla tpl : ls) {
                    if (aux3[0].equals(tpl.getV1())){
                        String valor = tpl.getV2();
                        valor = valor.substring(1, valor.length()-1);
                        String[] valores = valor.split(",");
                        return valores[Integer.parseInt(aux3[1])];
                    }
                }
            }

        }
        for (List<Tupla> ls : vars_globales) {
            for (Tupla tpl : ls) {
                if (ident.equals(tpl.getV1())) return tpl.getV2();
            }
        }
        return "";
    }


    //LIMPIA LAS VARIABLES LOCALES
    public void eliminaVarsLocales(){
        vars_globales.remove(vars_globales.size()-1);
        System.out.println(vars_globales);
    }

}
