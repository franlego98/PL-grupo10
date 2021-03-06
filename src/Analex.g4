lexer grammar Analex;

BLANCO: ' ' -> skip;
TABULADOR: '\t' -> skip;
FIN_LINEA: '\r'?'\n' -> skip;
COMENTARIO_BLOQUE: '/*' .*? '*/' -> skip ;
COMENTARIO_LINEA: '//' .*? FIN_LINEA -> skip ;


fragment DIGITO: [0-9];
fragment LETRA: [a-zA-Z];

//Seguros
VARIABLES: 'VARIABLES';
SUBPROGRAMAS: 'SUBPROGRAMAS';
FUNCION: 'FUNCION';
PROCEDIMIENTO: 'PROCEDIMIENTO';
DEV: 'dev';
INSTRUCCIONES: 'INSTRUCCIONES';
NUM: 'NUM';
LOG: 'LOG';
SEQ: 'SEQ';
SI: 'si';
ENTONCES: 'entonces';
SINO: 'sino';
FSI: 'fsi';
CIERTO: 'cierto';
FALSO: 'falso';
MIENTRAS: 'mientras';
HACER: 'hacer';
FMIENTRAS: 'fmientras';
RUPTURA: 'ruptura';
FFUNCION: 'FFUNCION';
FPROCEDIMIENTO: 'FPROCEDIMIENTO';
MOSTRAR: 'mostrar';

ASIG: '=';
CONJUNCION: '&&';
DISYUNCION: '||';
NEGACION: '!';
T: 'T';
F: 'F';
COMA:',';
PyC: ';';
DP: ':';
PA: '(';
PC: ')';
CA: '[';
CC: ']';
IGUAL: '==';
DESIGUAL: '!=';
MAYOR: '>';
MENOR: '<';
MAYORIGUAL: '>=';
MENORIGUAL: '<=';
MAS: '+';
MENOS: '-';
POR: '*';
VALOR: ('-')?DIGITO+;


IDENT: LETRA(LETRA | DIGITO  | '_')*;


