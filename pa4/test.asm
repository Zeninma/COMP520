  0         CALL         L10
  1         POP          0
  2         HALT   (0)   
  3  L10:   LOADL        -1
  4         LOADL        1
  5         CALL         newobj  
  6         LOAD         3[LB]
  7         CALLI        L11
  8         POP          1
  9         LOADL        1
 10         CALL         neg     
 11         CALL         putintnl
 12         POP          1
 13         RETURN (0)   1
 14  L11:   LOADA        0[OB]
 15         STORE        0[OB]
 16         RETURN (0)   0
 17         LOAD         -1[LB]
 18         LOADL        27
 19         CALL         add     
 20         CALL         putintnl
 21         RETURN (0)   1
