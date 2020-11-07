parser grammar Anasint;

options{
    tokenVocab = Analex;
}


programa: variables subprogramas instrucciones EOF;

//VARIABLES
variables: VARIABLES (decl_var PyC)*;

decl_var: identificador_declaracion DP tipo_de_dato;

identificador_declaracion: IDENT (COMA IDENT)*;

tipo_de_dato: tipo_elemental | tipo_no_elemental;

tipo_elemental: NUM | LOG;

tipo_no_elemental: SEQ PA (tipo_elemental) PC;

//SUBPROGRAMAS
subprogramas: SUBPROGRAMAS (funcion | procedimiento)*;

//SUBPROGRAMA FUNCION
funcion: FUNCION identificador_funcion variables instrucciones FFUNCION;

//SUBPROGRAMA PROCEDIMIENTO
procedimiento: PROCEDIMIENTO identificador_procedimiento variables instrucciones FPROCEDIMIENTO;

identificador_procedimiento: IDENT PA argumentos_subprograma PC;

identificador_funcion: IDENT PA (argumentos_subprograma)? PC DEV PA argumentos_subprograma PC;

argumentos_subprograma: (tipo_elemental | SEQ PA tipo_elemental PC ) IDENT (COMA (tipo_elemental | SEQ PA tipo_elemental PC ) IDENT)*;
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

identificador_variables: IDENT CA expresion_asignacion CC |
    IDENT
    ;

expresion_asignacion: expresion_asignacion1 ((MAS | MENOS | POR) (expresion_asignacion))? ;

expresion_asignacion1: T
    | F
    | IDENT
    | VALOR
    | IDENT CA expresion_asignacion CC
    | IDENT PA expresion_asignacion PC
    | PA expresion_asignacion PC
    ;

//INS CONDICION
ins_condicion: SI PA expresion_condicional PC ENTONCES tipo_instruccion+ (SINO tipo_instruccion+)? FSI;

//Condicionales
expresion_condicional: expresion_condicional1 ((CONJUNCION | DISYUNCION | igualdades) (expresion_condicional))?;

expresion_condicional1: CIERTO
    | FALSO
    | expresion_asignacion
    | NEGACION expresion_condicional
    | IDENT PA expresion_asignacion PC
    | PA expresion_condicional PC
    ;

igualdades: IGUAL | DESIGUAL | MAYOR | MENOR | MAYORIGUAL | MENORIGUAL;

//INS ITERACION
ins_iteracion: MIENTRAS PA expresion_condicional PC HACER tipo_instruccion+ FMIENTRAS;

//INS RUPTURA
ins_ruptura: RUPTURA PyC;

//INS DEVOLUCION
ins_devolucion: DEV expresion_asignacion (expresion_asignacion)* PyC;

//INS MOSTRAR
ins_mostrar: MOSTRAR PA expresion_asignacion (COMA expresion_asignacion)* PC PyC;