import java.util.ArrayList;
import java.util.List;

public class Interprete {

    List<List<Tupla>> vars_globales = new ArrayList<>();

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
