import java.util.ArrayList;
import java.util.List;

public class Interprete extends AnasintBaseVisitor<Integer>{

    List<List<Tupla>> vars_globales = new ArrayList<>();

    //VisitPrograma
    public Integer visitPrograma(Anasint.ProgramaContext ctx){
        visit(ctx.variables());
        visit(ctx.subprogramas());
        visit(ctx.instrucciones());
        return 0;
    }

    public Integer visitDecl_var(Anasint.Decl_varContext ctx){
        String ident;
        String tipo;
        for(String i : ctx.identificador_declaracion().getText().split(",")) {
            ident = i;
            tipo = ctx.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
        }
        return 0;
    }

    public Integer visitFuncion(Anasint.FuncionContext ctx){
        //Decision 1.2
        vars_globales.add(new ArrayList<Tupla>());
        visitIdentificador_funcion(ctx.identificador_funcion());
        return 0;
    }

    public Integer visitIdentificador_funcion(Anasint.Identificador_funcionContext ctx){
        String ident;
        String tipo;
        for(Anasint.Argumento_subprogramaContext i : ctx.argumento_subprograma()) {
            ident = i.IDENT().getText();
            tipo = i.tipo_de_dato().getText();
            declarar_variable(ident, tipo);
        }

        return 0;
    }

    //GUARDAR UNA VARIABLE AL DECLARARLA
    void declarar_variable(String ident, String tipo){
        String valor;
        if(tipo == "NUM" || tipo == "LOG") valor = "0";
        else valor = "[]";

        vars_globales.get(-1).add(new Tupla(ident,valor));
    }

    //ACTUALIZAR UNA VARIABLE CON UN NUEVO VALOR
    void actualizar_valor(String ident, String valor){
        for (List<Tupla> ls: vars_globales){
            for(Tupla tpl : ls) {
                if (ident == tpl.getV1()) tpl.putV2(valor);
            }
        }
    }

    //BUSCA Y DEVUELVE EL VALOR DE UNA VARIABLE
    String devolver_valor(String ident) {
        for (List<Tupla> ls : vars_globales) {
            for (Tupla tpl : ls) {
                if (ident == tpl.getV1()) return tpl.getV2();
            }
        }
        return null;
    }
}
