parser grammar Anasint;

options{
    tokenVocab = Analex;
}

programa: variables subprogramas instrucciones EOF;

//VARIABLES
variables: VARIABLES (decl_var PyC)*;

decl_var: identificador_declaracion DP tipo_de_dato;

identificador_declaracion: IDENT (COMA IDENT)*;

tipo_de_dato: NUM
    | LOG
    | SEQ PA NUM PC
    | SEQ PA LOG PC
    ;

//SUBPROGRAMAS
subprogramas: SUBPROGRAMAS (funcion | procedimiento)*;

//SUBPROGRAMA FUNCION
funcion: FUNCION identificador_funcion variables instrucciones FFUNCION;

identificador_funcion: IDENT PA (argumentos_subprograma)? PC DEV PA argumentos_subprograma PC;

//SUBPROGRAMA PROCEDIMIENTO
procedimiento: PROCEDIMIENTO identificador_procedimiento variables instrucciones FPROCEDIMIENTO;

identificador_procedimiento: IDENT PA argumentos_subprograma PC;

//Parte común funcion y procedimiento
argumentos_subprograma: tipo_de_dato IDENT (COMA tipo_de_dato IDENT)*;

//INSTRUCCIONES
instrucciones: INSTRUCCIONES (tipo_instruccion)+;

tipo_instruccion: ins_asignacion
    | ins_condicion
    | ins_iteracion
    | ins_ruptura
    | ins_devolucion
    | ins_mostrar
    ;

//INS ASIGNACION
ins_asignacion: identificador_variables (COMA identificador_variables)* ASIG expresion_asignacion (COMA expresion_asignacion)* PyC;

//Operando de la izquierda
identificador_variables: IDENT CA expresion_asignacion CC |
    IDENT
    ;

//Operando de la derecha
expresion_asignacion: expresion_asignacion1 (operadores_aritmeticos (expresion_asignacion))? ;

operadores_aritmeticos: MAS | MENOS | POR ;

expresion_asignacion1: T #AsigTrue
    | F #AsigFalse
    | IDENT #AsigSimple
    | VALOR #AsigExplicit
    | IDENT CA expresion_asignacion CC #AsigLista
    | IDENT PA expresion_asignacion PC #AsignFunc
    | PA expresion_asignacion PC #AsigParentesis
    ;

//INS CONDICION
ins_condicion: SI PA expresion_condicional PC ENTONCES tipo_instruccion+ (SINO tipo_instruccion+)? FSI;

//Condicionales
expresion_condicional: expresion_condicional1 (operadores_binarios (expresion_condicional))?;

expresion_condicional1: CIERTO #CondCierto
    | FALSO #CondFalse
    | expresion_asignacion #CondVar
    | NEGACION expresion_condicional #CondNegacion
    | PA expresion_condicional PC #CondParentesis
    ;

operadores_binarios: CONJUNCION | DISYUNCION | IGUAL | DESIGUAL | MAYOR | MENOR | MAYORIGUAL | MENORIGUAL;

//INS ITERACION
ins_iteracion: MIENTRAS PA expresion_condicional PC HACER tipo_instruccion+ FMIENTRAS;

//INS RUPTURA
ins_ruptura: RUPTURA PyC;

//INS DEVOLUCION
ins_devolucion: DEV expresion_asignacion (COMA expresion_asignacion)* PyC;

//INS MOSTRAR
ins_mostrar: MOSTRAR PA expresion_asignacion (COMA expresion_asignacion)* PC PyC;