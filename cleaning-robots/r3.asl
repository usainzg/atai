// Agent r3 in project mars.mas2j

!wander(S).

+!make_garb(S): not garbage(r3) 
	<- 
    ?pos(r3, X, Y);
	gen_garb(X, Y);
	!wander(S).

+!make_garb(S) 
    <- 
    !wander(S).
		   
+!wander(S) 
    <- 
    move_around(S);  
    !make_garb(S);
    !wander(S).