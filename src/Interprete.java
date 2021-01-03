import java.util.ArrayList;
import java.util.List;

public class Interprete extends AnasintBaseVisitor<Integer>{

    List<List<Tupla>> vars_globales = new ArrayList<>();

    //FUNCIONA
    //VisitPrograma
    public Integer visitPrograma(Anasint.ProgramaContext ctx){
        vars_globales.add(new ArrayList<Tupla>());
        visit(ctx.variables());
        visit(ctx.subprogramas());
        visit(ctx.instrucciones());
        return 0;
    }

    //FUNCIONA
    public Integer visitDecl_var(Anasint.Decl_varContext ctx){
        String ident;
        String tipo;
        for(String i : ctx.identificador_declaracion().getText().split(",")) {
            ident = i;
            tipo = ctx.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
            System.out.println(vars_globales);
        }
        return 0;
    }

    //FUNCIONA
    public Integer visitFuncion(Anasint.FuncionContext ctx){
        //Decision 1.2
        vars_globales.add(new ArrayList<Tupla>());
        visitIdentificador_funcion(ctx.identificador_funcion());
        visit(ctx.variables());
        visit(ctx.instrucciones());
        return 0;
    }
    //FUNCIONA
    public Integer visitIdentificador_funcion(Anasint.Identificador_funcionContext ctx){
        String ident;
        String tipo;
        for(Anasint.Argumento_subprogramaContext i : ctx.argumento_subprograma()) {
            ident = i.IDENT().getText();
            tipo = i.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
            System.out.println(vars_globales);
        }

        return 0;
    }
    //FUNCIONA
    public Integer visitProcedimiento(Anasint.ProcedimientoContext ctx){
        //Decision 1.2
        vars_globales.add(new ArrayList<Tupla>());
        visitIdentificador_procedimiento(ctx.identificador_procedimiento());
        visit(ctx.variables());
        visit(ctx.instrucciones());
        return 0;
    }

    //FUNCIONA
    public Integer visitIdentificador_procedimiento(Anasint.Identificador_procedimientoContext ctx){
        String ident;
        String tipo;
        for(Anasint.Argumento_subprogramaContext i : ctx.argumento_subprograma()) {
            ident = i.IDENT().getText();
            tipo = i.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
            System.out.println(vars_globales);
        }

        return 0;
    }
    //INSTRUCCIONES

    //FUNCIONA
    public Integer visitInstrucciones(Anasint.InstruccionesContext ctx){

        for (int i = 0; i < ctx.tipo_instruccion().size(); i++){
            System.out.println(ctx.tipo_instruccion(i).getText());
            visit(ctx.tipo_instruccion(i));

        }
        return 0;
    }

    //CASI FUNCIONA
    public Integer visitTipoInstruccion(Anasint.Tipo_instruccionContext ctx){

        String ti = ctx.getText();
        switch (ti){
            case "ins_asignacion":
                visitIns_asignacion(ctx.ins_asignacion());
                break;
            case "ins_condicion":
                visitIns_condicion(ctx.ins_condicion());
                break;
            case "ins_iteracion":
                visitIns_itera(ctx.ins_iteracion());
                break;
            case "ins_ruptura":
                break;
            case "ins_devolucion":
                visitIns_dev(ctx.ins_devolucion());
                break;
            case "ins_mostrar":
                visitIns_mostrar(ctx.ins_mostrar());
                break;
        }
        return 0;
    }

    //FUNCIONA
    public Integer visitIns_asignacion(Anasint.Ins_asignacionContext ctx){

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


        return 0;
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
            res = visitExpresion_asignacion1(ctx.expresion_asignacion1());
        }

        return res;
    }

    //CASI LISTA
    public String visitExpresion_asignacion1(Anasint.Expresion_asignacion1Context ctx){
        String exp = ctx.getText();
        String res = "";
        //Es un bool?
        if(exp == "T" || exp == "F"){
            res = exp;
        }else if(exp.endsWith("]")){
            if(exp.startsWith("[")){
                res = exp;
            }
            else{
                res = devolver_valor(exp);
            }
        }else if(exp.endsWith(")")){
            if(exp.startsWith("(")){
                //POR HACER
                //POR HACER
                //POR HACER
            }else{

            }
        }else if(Character.isDigit(exp.charAt(0))){
            res = exp;

        }

        return res;
    }

    public Integer visitIns_condicion(Anasint.Ins_condicionContext ctx){

        Boolean condicion = visitExpr_cond(ctx.expresion_condicional());
        List<Anasint.Tipo_instruccionContext> ins = ctx.tipo_instruccion();
        List<Anasint.Tipo_instruccion2Context> ins2 = ctx.tipo_instruccion2();

        if(condicion){
            for (int i = 0; i<ins.size();i++){
                visitTipo_instruccion(ctx.tipo_instruccion(i));
            }
        }else{
            for (int i = 0; i<ins.size();i++){
                visitTipo_instruccion(ctx.tipo_instruccion2(i).tipo_instruccion());
            }
        }

        return 0;
    }

    public Boolean visitExpr_cond(Anasint.Expresion_condicionalContext ctx){

        Boolean condicion = false;
        String s = visit(ctx.expresion_condicional1()).toString();

        if(ctx.operadores_binarios() != null){
            Boolean condicionD = visitExpr_cond(ctx.expresion_condicional());
            String s2 = visit(ctx.expresion_condicional().expresion_condicional1()).toString();
            switch (ctx.operadores_binarios().getText()){
                case "CONJUNCION":
                    condicion = (Boolean.parseBoolean(s)) && condicionD;
                    break;
                case "DISYUNCION":
                    condicion = (Boolean.parseBoolean(s)) || condicionD;
                    break;
                case "IGUAL":
                    condicion = (s == s2);
                    break;
                case "DESIGUAL":
                    condicion = (s != s2);
                    break;
                case "MAYOR":
                    condicion = (Integer.parseInt(s) > Integer.parseInt(s2));
                    break;
                case "MENOR":
                    condicion = (Integer.parseInt(s) < Integer.parseInt(s2));
                    break;
                case "MAYORIGUAL":
                    condicion = (Integer.parseInt(s) >= Integer.parseInt(s2));
                    break;
                case "MENORIGUAL":
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

    public Integer visitIns_itera(Anasint.Ins_iteracionContext ctx){

        Boolean condicion = visitExpr_cond(ctx.expresion_condicional());
        List<Anasint.Tipo_instruccionContext> ins = ctx.tipo_instruccion();

        if(condicion){
            for(int i = 0; i<ins.size(); i++) {
                if(ctx.tipo_instruccion(i).equals(Anasint.RUPTURA)){
                    return 0;
                }
                visitTipoInstruccion(ctx.tipo_instruccion(i));
            }
        }

        return 0;
    }

    public Integer visitIns_dev(Anasint.Ins_devolucionContext ctx){

        List<Anasint.Expresion_asignacionContext> as = ctx.expresion_asignacion();

        for(int i = 0; i<as.size(); i++){
            visit(ctx.expresion_asignacion(i));
        }

        return 0;
    }

    //FUNCIONA
    public Integer visitIns_mostrar(Anasint.Ins_mostrarContext ctx){

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
        return 0;
    }

    //FUNCIONAN TODAS!!
    //FUNCIONES UTILES


    //GUARDAR UNA VARIABLE AL DECLARARLA
    void declarar_variable(String ident, String tipo){
        String valor;
        if(tipo.equals("NUM")) valor = "0";
        else if(tipo.equals("LOG")) valor = "T";
        else valor = "[]";
        vars_globales.get(vars_globales.size()-1).add(new Tupla(ident,valor));
    }

    //ACTUALIZAR UNA VARIABLE CON UN NUEVO VALOR
    void actualizar_valor(String ident, String valor){
        for (List<Tupla> ls: vars_globales){
            for(Tupla tpl : ls) {
                if (ident.equals(tpl.getV1())) tpl.putV2(valor);
            }
        }
    }

    //EXISTE LA VARIABLE?
    Boolean existe(String ident){
        for (List<Tupla> ls : vars_globales) {
            for (Tupla tpl : ls) {
                if (ident.equals(tpl.getV1())) return true;
            }
        }
        return false;
    }

    //BUSCA Y DEVUELVE EL VALOR DE UNA VARIABLE
    String devolver_valor(String ident) {
        //ELEMENTO DE UNA LISTA?
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



}
