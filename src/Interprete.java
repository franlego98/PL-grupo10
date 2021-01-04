import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Interprete extends AnasintBaseVisitor<String>{

    //ALMACEN DE VARIABLES
    List<List<Tupla>> vars_globales = new ArrayList<>();

    //ALMACEN DE FUNCIONES Y PROCEDIMIENTOS
    HashMap<String,Anasint.FuncionContext> funcion_globales = new HashMap<String, Anasint.FuncionContext>();
    HashMap<String,Anasint.ProcedimientoContext> procedimiento_globales = new HashMap<String, Anasint.ProcedimientoContext>();


    public String visitPrograma(Anasint.ProgramaContext ctx){
        vars_globales.add(new ArrayList<Tupla>());
        visit(ctx.variables());
        visit(ctx.subprogramas());
        visit(ctx.instrucciones());
        return "";
    }

    public String visitDecl_var(Anasint.Decl_varContext ctx){
        String ident;
        String tipo;
        for(String i : ctx.identificador_declaracion().getText().split(",")) {
            ident = i;
            tipo = ctx.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
        }
        return "";
    }

    public String visitSubprogramas(Anasint.SubprogramasContext ctx){
        for(Anasint.FuncionContext sp : ctx.funcion()){
            funcion_globales.put(sp.identificador_funcion().IDENT().getText(), sp);
        }
        for(Anasint.ProcedimientoContext sp : ctx.procedimiento()){
            procedimiento_globales.put(sp.identificador_procedimiento().IDENT().getText(),sp);
        }

        return "";
    }

    public String visitFuncion(Anasint.FuncionContext ctx, List<String> valores){
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
        }
        return "";
    }

    public String visitProcedimiento(Anasint.ProcedimientoContext ctx, List<String> valores){
        vars_globales.add(new ArrayList<Tupla>());
        visitIdentificador_procedimiento(ctx.identificador_procedimiento(),valores);
        visit(ctx.variables());
        visit(ctx.instrucciones());
        return "";
    }
    public String visitIdentificador_procedimiento(Anasint.Identificador_procedimientoContext ctx, List<String> valores){
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
        }
        return "";
    }

    public String visitIdentificador_procedimiento(Anasint.Identificador_procedimientoContext ctx){
        String ident;
        String tipo;
        for(Anasint.Argumento_subprogramaContext i : ctx.argumento_subprograma()) {
            ident = i.IDENT().getText();
            tipo = i.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
        }

        return "";
    }

    //INSTRUCCIONES

    public String visitInstrucciones(Anasint.InstruccionesContext ctx){

        String res = "";

        for (int i = 0; i < ctx.tipo_instruccion().size(); i++){
            if(ctx.tipo_instruccion(i).equals(Anasint.RUPTURA)){
                return "";
            }else {
                res = visit(ctx.tipo_instruccion(i));
            }
        }
        return res;
    }

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
            String[] sp = esxp.split(",");
            if(esxp.startsWith("[")){
                vals.add(esxp);
            }else if(sp.length>1){
                for(int i = 0; i < sp.length; i++){
                    vals.add(sp[i]);
                }
            }else{
                vals.add(esxp);
            }
        }

        for(int i = 0; i < ids.size(); i++){
            actualizar_valor(ids.get(i), vals.get(i));
        }

        return "";
    }

    public String visitExpr_asig(Anasint.Expresion_asignacionContext ctx){

        String res = "";
        Integer n = 0;
        if(ctx.operadores_aritmeticos()!=null){
            String exp1 = ctx.expresion_asignacion1().getText();
            if(es_un_ident(exp1)){
                n = Integer.parseInt(devolver_valor(exp1));
            }else {
                n = Integer.parseInt(exp1);
            }
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
    public String visitAsigTrue(Anasint.AsigTrueContext ctx) {
        return "T";
    }
    public String visitAsigFalse(Anasint.AsigFalseContext ctx) {
        return "F";
    }
    public String visitAsigExplicit(Anasint.AsigExplicitContext ctx) {
        return ctx.getText();
    }
    public String visitAsigLista(Anasint.AsigListaContext ctx) {
        return devolver_valor(ctx.getText());
    }
    public String visitAsignFunc(Anasint.AsignFuncContext ctx) {
        Anasint.FuncionContext funcion = funcion_globales.get(ctx.IDENT().getText());
        List<String> valores = new ArrayList<>();
        for(Anasint.Expresion_asignacionContext exp : ctx.expresion_asignacion()){
            valores.add(visit(exp));
        }
        return visitFuncion(funcion,valores);
    }
    public String visitAsigParentesis(Anasint.AsigParentesisContext ctx) {
        return visit(ctx.expresion_asignacion());
    }
    public String visitAsigExplicitLista(Anasint.AsigExplicitListaContext ctx) {
        String res = "[";
        for(Anasint.Expresion_asignacionContext exp : ctx.expresion_asignacion()){
            res += visit(exp) +",";
        }
        res = res.substring(0,res.length()-1)+"]";
        return res;
    }
    public String visitAsigSimple(Anasint.AsigSimpleContext ctx) {
        return devolver_valor(ctx.getText());
    }

    public String visitIns_condicion(Anasint.Ins_condicionContext ctx){

        Boolean condicion = visitExpr_cond(ctx.expresion_condicional());
        List<Anasint.Tipo_instruccionContext> ins = ctx.tipo_instruccion();
        List<Anasint.Tipo_instruccion2Context> ins2 = ctx.tipo_instruccion2();

        if(condicion){
            for (int i = 0; i<ins.size();i++){
                if(ctx.tipo_instruccion(i).equals(Anasint.RUPTURA)){
                    return "";
                }else{
                    visitTipo_instruccion(ctx.tipo_instruccion(i));
                }
            }
        }else{
            for (int i = 0; i<ins2.size();i++){
                if(ctx.tipo_instruccion(i).equals(Anasint.RUPTURA)){
                    return "";
                }else{
                    visitTipo_instruccion(ctx.tipo_instruccion2(i).tipo_instruccion());
                }
            }
        }

        return "";
    }
    public String visitCondCierto(Anasint.CondCiertoContext ctx) {
        return "T";
    }
    public String visitCondFalse(Anasint.CondFalseContext ctx) {
        return "F";
    }
    public String visitCondVar(Anasint.CondVarContext ctx) {
        return visit(ctx.expresion_asignacion());
    }
    public String visitCondNegacion(Anasint.CondNegacionContext ctx) {
        String s = ctx.expresion_condicional().getText();
        if (s.equals("cierto")) return "F";
        return "T";
    }
    public String visitCondParentesis(Anasint.CondParentesisContext ctx) {
        return visit(ctx.expresion_condicional());
    }

    public Boolean visitExpr_cond(Anasint.Expresion_condicionalContext ctx){

        Boolean condicion = true;
        String s = visit(ctx.expresion_condicional1());

        if(es_un_ident(s)) s = devolver_valor(s);

        if(ctx.operadores_binarios() != null){

            Boolean condicionI = false;
            if (s.equals("T")) condicionI = true;

            String s2 = visit(ctx.expresion_condicional().expresion_condicional1());
            if(es_un_ident(s2)) s2 = devolver_valor(s2);


            Boolean condicionD = true;
            if (s2.equals("F")) condicionD = false;

            switch (ctx.operadores_binarios().getText()){
                case "&&":
                    condicion = condicionI && condicionD;
                    break;
                case "||":
                    condicion = condicionI || condicionD;
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

    public String visitIns_iteracion(Anasint.Ins_iteracionContext ctx) {

        Boolean condicion = visitExpr_cond(ctx.expresion_condicional());
        List<Anasint.Tipo_instruccionContext> ins = ctx.tipo_instruccion();

        if(condicion){
            for(int i = 0; i<ins.size(); i++) {
                if(ctx.tipo_instruccion(i).equals(Anasint.RUPTURA)){
                    return "";
                }
                visit(ctx.tipo_instruccion(i));
            }
            visitIns_iteracion(ctx);
        }
        return "";
    }

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

    public String visitIns_mostrar(Anasint.Ins_mostrarContext ctx){

        List<Anasint.Expresion_asignacionContext> as = ctx.expresion_asignacion();
        String res = "";
        for(int i = 0; i<as.size(); i++){
            if(existe(as.get(i).getText())) {
                res += as.get(i).getText() + " = " + devolver_valor(as.get(i).getText())+ ", ";
            }
            else{
                res += as.get(i).getText() + " = " + visitExpr_asig(as.get(i))+ ", ";
            }
        }
        res = res.substring(0,res.length()-2);
        System.out.println(res);
        return "";
    }

    public String visitIns_ruptura(Anasint.Ins_rupturaContext ctx) {
        return "";
    }

    public String visitIns_procedimiento(Anasint.Ins_procedimientoContext ctx) {
        Anasint.ProcedimientoContext proc = procedimiento_globales.get(ctx.IDENT().getText());
        List<String> valores = new ArrayList<>();
        for(Anasint.Expresion_asignacionContext exp : ctx.expresion_asignacion()){
            valores.add(visit(exp));
        }
        visitProcedimiento(proc,valores);
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
    }

}
