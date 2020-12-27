import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InterpretaInstrucciones extends AnasintBaseVisitor<Integer>{

    public static List<List<Tupla>> varsLocales;

    public Integer visiTipoInstruccion(Anasint.Tipo_instruccionContext ctx){

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
                visitIns_most(ctx.ins_mostrar());
                break;
        }
        return 0;
    }

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


        // Los dos siguientes for son para actualizar las variables globales con los valores de las asignaciones
        // Estas asignaciones se han guardado en las listas ids y vals, siendo estas los V1 y V2 en las tuplas
        // respectivamente
        List<String> id = new ArrayList<String>();
        for (List<Tupla> l:varsLocales){
            for (Tupla t:l){
                id.add(t.getV1());
            }
        }

        for (List<Tupla> l: varsLocales){
            for (int i = 0; i<ids.size();i++){
                String var = ids.get(i);
                Integer index = id.indexOf(var);
                Tupla t = new Tupla(var, vals.get(i));
                l.remove(index);
                l.add(index,t);
            }
        }

        return 0;
    }

    public String visitExpr_asig(Anasint.Expresion_asignacionContext ctx){

        String res = "";
        Integer n = visit(ctx.expresion_asignacion1());;

        if(ctx.operadores_aritmeticos()!=null){
            switch (ctx.operadores_aritmeticos().getText()){
                case "MAS":
                    n += visit(ctx.expresion_asignacion());
                    break;
                case "MENOS":
                    n -= visit(ctx.expresion_asignacion());
                    break;
                case "POR":
                    n *= visit(ctx.expresion_asignacion());
                default:
                    break;
            }
        }
        res = n.toString();

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
                visiTipoInstruccion(ctx.tipo_instruccion(i));
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

    public Integer visitIns_most(Anasint.Ins_mostrarContext ctx){

        List<Anasint.Expresion_asignacionContext> as = ctx.expresion_asignacion();

        for(int i = 0; i<as.size(); i++){
            visit(ctx.expresion_asignacion(i));
            System.out.println(i);
        }

        return 0;
    }

}
