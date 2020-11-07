parser grammar Anasint;

options{
    tokenVocab = Analex;
}


programa: variables subprogramas instrucciones;

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

identificador_funcion: IDENT PA argumentos_subprograma PC DEV PA argumentos_subprograma PC;

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

expresion_asignacion: expresion_asignacion1
    | PA expresion_asignacion1 PC
    ;

expresion_asignacion1:
    (expresion_asignacion2 | PA expresion_asignacion2 PC) ((MAS | MENOS | POR) (expresion_asignacion2 | PA expresion_asignacion2 PC))*
    | IDENT CA expresion_asignacion2 CC
    | IDENT PA expresion_asignacion2 PC
    ;

expresion_asignacion2: CA VALOR (COMA VALOR)* CC
    | VALOR
    | IDENT
    | T
    | F
    ;

//INS CONDICION
ins_condicion: SI PA expresion_condicional PC ENTONCES tipo_instruccion+ (SINO tipo_instruccion+)? FSI;

expresion_condicional: expresion_condicional1
    | PA expresion_condicional1 PC
    ;

expresion_condicional1:
    (expresion_condicional2 | PA expresion_condicional2 PC) ((CONJUNCION | DISYUNCION | igualdades) (expresion_condicional2 | PA expresion_condicional2 PC))*
    | NEGACION expresion_condicional2
    | IDENT PA expresion_asignacion2 PC
    ;

expresion_condicional2:  CIERTO
    | FALSO
    | IDENT
    ;

igualdades: IGUAL | DESIGUAL | MAYOR | MENOR | MAYORIGUAL | MENORIGUAL;

//INS ITERACION
ins_iteracion: MIENTRAS PA expresion_condicional PC HACER tipo_instruccion+ FMIENTRAS;

//INS RUPTURA
ins_ruptura: RUPTURA PyC;

//INS DEVOLUCION
ins_devolucion: DEV expresion_asignacion (expresion_asignacion)* PyC;

//INS MOSTRAR
ins_mostrar: MOSTRAR PA expresion_asignacion (COMA expresion_asignacion)* PC;