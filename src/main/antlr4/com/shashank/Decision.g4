grammar Decision;


range               :  '[' NUMBER TO NUMBER ']'                                  #rangeNumberToNumber
                    |  '[' WILDCARD '-' NUMBER ']'                               #rangeWildcardToNumber
                    |  '[' NUMBER '-' WILDCARD ']'                               #rangeNumberToWildCard
                    ;


rangeNumberlists    : '[' NUMBER numbers?']'                                     #numbersListArr
                    ;

rangeStringlists    : '[' STRING strings?']'                                     #stringListArr
                    ;

expression          : '(' expression ')'                                         #parenthesisExp
                    | expression (AND|OR) expression                             #andOrExp
                    | NUMBER BELONGS range                                       #numberRangeExp
                    | VARNAME BELONGS range                                      #varNameRangeExp
                    | NUMBER BELONGS rangeNumberlists                            #numberRangeListExp
                    | VARNAME BELONGS rangeNumberlists                           #varNameRangeListExp
                    | rangeNumberlists EXACTMATCH  rangeNumberlists              #numberMatchExp
                    | rangeStringlists EXACTMATCH  rangeStringlists              #stringMatchExp
                    | VARNAME BELONGS rangeStringlists                           #varNameBelongsStringExp
                    | VARNAME EXACTMATCH rangeNumberlists                        #varNameMatchNumberExp
                    | VARNAME EXACTMATCH rangeStringlists                        #varNameMatchStringExp
                    ;


numbers             : COMMA NUMBER numbers*                                      #numbersList
                    ;

strings             : COMMA STRING strings*                                      #stringsList
                    ;


fragment LETTER     : [a-zA-Z] ;
fragment DIGIT      : [0-9] ;

COMMA               : ',' ;
TO                  : '-' ;
AND                 : '&' ;
OR                  : '||';
BELONGS             : '%' ;
WILDCARD            : '*' ;
DOLLAR              : '$' ;
EXACTMATCH          : '==';
HASH                : '#' ;

NAME				: LETTER+ ;
STRING              : HASH LETTER+ ;
VARNAME             : DOLLAR LETTER+ ;
NUMBER              : DIGIT+ ('.' DIGIT+)? ;
WS                  : [ \t\n\r]+ -> skip ;
//WHITESPACE          : ' ' -> channel(HIDDEN);