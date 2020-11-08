import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class AnasemVisitor extends AnasintBaseVisitor<Integer> {
    public final Integer TIPO_NUM = 100;
    public final Integer TIPO_LOG = 101;

    HashMap<String,String> vars_global = new HashMap<String, String>();

    @Override
    public Integer visitPrograma(Anasint.ProgramaContext ctx){
        visit(ctx.variables());
        System.out.println(vars_global.toString());
        visit(ctx.subprogramas());
        visit(ctx.instrucciones());
        return 0;
    }

    public Integer visitDecl_var(Anasint.Decl_varContext ctx) {

        //Decision de diseño 2
        for(String i : ctx.identificador_declaracion().getText().split(",")) {
            if (vars_global.containsKey(i)){
                System.out.println("Variable "+i+" declarada previamente como "+vars_global.get(i));
                continue;
            }
            vars_global.put(i,ctx.tipo_de_dato().getText());
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
        return -1;
    }

    @Override
    public Integer visitAsigParentesis(Anasint.AsigParentesisContext ctx) {
        return visit(ctx.expresion_asignacion());
    }
}