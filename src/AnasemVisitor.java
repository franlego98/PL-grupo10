import org.antlr.v4.runtime.tree.ParseTree;

public class AnasemVisitor extends AnasintBaseVisitor<Integer> {

    public Integer visitPrograma(Anasint.ProgramaContext ctx){
        visit(ctx.variables());
        return 0;
    }

    public Integer visitVariables(Anasint.VariablesContext ctx){
        for(int i = 0;i < ctx.children.size();i++){
            visit(ctx.children.get(i));
        }
        return 0;
    }

    public Integer visitDecl_Var(Anasint.Decl_varContext ctx) {
        System.out.println(ctx.identificador_declaracion().getText());
        return 0;
    }
}