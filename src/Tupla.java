public class Tupla {
    String var,valor;

    public Tupla(String p1, String p2) {
        super();
        this.var = p1;
        this.valor = p2;
    }
    public String getV1(){
        return var;
    }
    public String getV2(){
        return valor;
    }
    public void putV1(String v) {
        this.var = v;
    }
    public void putV2(String v) {
        this.valor = v;
    }

    public String toString(){
        return "("+var+","+valor+")";
    }
}
