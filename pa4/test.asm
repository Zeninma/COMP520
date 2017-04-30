  0         PUSH         1
  1         CALL         L10
  2         POP          1
  3         HALT   (0)   
  4  L10:   LOADL        1
  5         LOADL        2
  6         LOAD         3[LB]
  7         CALL         mult    
  8         LOAD         3[LB]
  9         CALL         add     
 10         LOADL        1
 11         CALL         sub     
 12         STORE        3[LB]
 13         LOADL        3
 14         CALL         putintnl
 15         LOAD         3[LB]
 16         LOADL        1
 17         CALL         neg     
 18         CALL         ne      
 19         JUMPIF (0)   L11
 20         LOADL        4
 21         CALL         putintnl
 22         JUMP         L12
 23  L11:   LOADL        1
 24         CALL         neg     
 25         CALL         putintnl
 26  L12:   LOADL        0
 27  L13:   LOAD         4[LB]
 28         LOADL        5
 29         CALL         lt      
 30         JUMPIF (0)   L14
 31         LOAD         4[LB]
 32         LOADL        1
 33         CALL         add     
 34         STORE        4[LB]
 35         LOAD         4[LB]
 36         STORE        3[LB]
 37         POP          0
 38         JUMP         L13
 39  L14:   LOAD         3[LB]
 40         CALL         putintnl
 41         LOADL        -1
 42         LOADL        2
 43         CALL         newobj  
 44         LOAD         5[LB]
 45         LOADL        0
 46         CALL         ne      
 47         JUMPIF (0)   L15
 48         LOADL        6
 49         CALL         putintnl
 50         JUMP         L15
 51  L15:   LOADL        7
 52         LOAD         5[LB]
 53         LOADL        0
 54         CALL         fieldref
 55         CALL         add     
 56         STORE        3[LB]
 57         LOAD         3[LB]
 58         CALL         putintnl
 59         LOAD         5[LB]
 60         LOADL        1
 61         LOADL        -1
 62         LOADL        2
 63         CALL         newobj  
 64         CALL         fieldupd
 65         LOAD         5[LB]
 66         LOADL        1
 67         CALL         fieldref
 68         LOADL        0
 69         LOADL        8
 70         CALL         fieldupd
 71         LOAD         5[LB]
 72         LOADL        1
 73         CALL         fieldref
 74         LOADL        0
 75         CALL         fieldref
 76         CALL         putintnl
 77         LOADL        4
 78         CALL         newarr  
 79         LOAD         6[LB]
 80         CALL         arraylen
 81         STORE        3[LB]
 82         LOADL        2
 83         LOAD         3[LB]
 84         CALL         mult    
 85         LOADL        1
 86         CALL         add     
 87         CALL         putintnl
 88         LOAD         6[LB]
 89         LOADL        0
 90         LOADL        0
 91         CALL         arrayupd
 92         LOADL        1
 93         STORE        4[LB]
 94  L16:   LOAD         4[LB]
 95         LOAD         6[LB]
 96         CALL         arraylen
 97         CALL         lt      
 98         JUMPIF (0)   L17
 99         LOAD         6[LB]
100         LOAD         4[LB]
101         LOAD         6[LB]
102         LOAD         4[LB]
103         LOADL        1
104         CALL         sub     
105         CALL         arrayref
106         LOAD         4[LB]
107         CALL         add     
108         CALL         arrayupd
109         LOAD         4[LB]
110         LOADL        1
111         CALL         add     
112         STORE        4[LB]
113         POP          0
114         JUMP         L16
115  L17:   LOAD         6[LB]
116         LOADL        3
117         CALL         arrayref
118         LOADL        4
119         CALL         add     
120         STORE        3[LB]
121         LOAD         3[LB]
122         CALL         putintnl
123         LOAD         5[LB]
124         CALLI        L18
125         LOADL        999
126         CALL         putintnl
127         RETURN (0)   1
128  L18:   LOADL        11
129         LOAD         3[LB]
130         CALL         putintnl
131         LOAD         1[OB]
132         LOADL        1
133         LOADA        0[OB]
134         CALL         fieldupd
135         LOADL        12
136         STORE        0[OB]
137         LOAD         1[OB]
138         LOADL        1
139         CALL         fieldref
140         LOADL        0
141         CALL         fieldref
142         STORE        3[LB]
143         LOAD         3[LB]
144         CALL         putintnl
145         LOADL        4
146         STORE        0[OB]
147         LOADL        2
148         LOADL        3
149         LOADL        4
150         LOADA        0[OB]
151         CALLI        L19
152         CALL         add     
153         STORE        3[LB]
154         LOAD         3[LB]
155         CALL         putintnl
156         LOADL        8
157         LOADL        3
158         LOAD         1[OB]
159         CALLI        L21
160         CALL         add     
161         CALL         putintnl
162         LOADA        0[OB]
163         LOADL        0
164         LOADL        4
165         CALL         fieldupd
166         LOAD         1[OB]
167         LOADL        0
168         LOADL        5
169         CALL         fieldupd
170         LOADL        2
171         LOADA        0[OB]
172         LOADA        0[OB]
173         LOADL        1
174         CALL         fieldref
175         LOADA        0[OB]
176         CALLI        L20
177         CALL         add     
178         CALL         putintnl
179         RETURN (0)   0
180  L19:   LOAD         0[OB]
181         LOAD         -1[LB]
182         CALL         add     
183         LOAD         -2[LB]
184         CALL         add     
185         RETURN (1)   2
186  L20:   LOAD         -1[LB]
187         LOADL        0
188         CALL         fieldref
189         LOAD         -2[LB]
190         LOADL        0
191         CALL         fieldref
192         CALL         add     
193         LOADA        0[OB]
194         LOADL        0
195         CALL         fieldref
196         CALL         add     
197         RETURN (1)   2
198  L21:   LOADL        1
199         LOAD         -1[LB]
200         LOADL        1
201         CALL         gt      
202         JUMPIF (0)   L22
203         LOAD         -1[LB]
204         LOAD         -1[LB]
205         LOADL        1
206         CALL         sub     
207         LOADA        0[OB]
208         CALLI        L21
209         CALL         mult    
210         STORE        3[LB]
211         JUMP         L22
212  L22:   LOAD         3[LB]
213         RETURN (1)   1
