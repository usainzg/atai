// mars robot 1

/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r1,X,Y).

battery(30).

/* Initial goal */

!check(slots).

/* Plans */
+!check(slots) : not garbage(r1) & battery(L) & L > 0 & not charge
   <- 
   next(slot);
   -+battery(L-1);
   show_battery(L-1);
   !check(slots).
+!check(slots).

+!check(slots) : battery(L) & L == 0 & not charge.
+!check(slots).

/* Battery logic */
+battery(0) : true
   <-
   -at(_);
   -garbage(r1);
   - pos(last, X, Y);
   .drop_all_intentions;
   !charge_agent(charge).

+!charge_agent(charge) : true
   <-
   ?pos(r1, X, Y);
   -+pos(last, X, Y);
   !at(r4);
   +charge;
   !charging(r);
   !at(last);
   -at(_);
   -garbage(r1);
   -charge;
   .drop_all_intentions;
   !check(slots).

+!charging(r) : battery(L) & L < 100 & charge
   <-
   -+battery(L+1);
   show_battery(L+1);
   .wait(100);
   !charging(r).

+!charging(r) : battery(L) & L == 100 & charge.


@lg[atomic]
+garbage(r1) : not .desire(carry_to(r2)) & battery(L) & L > 0 & not charge
   <- !carry_to(r2).

+!carry_to(R) : battery(L) & L > 0 & not charge
   <- // remember where to go back
      ?pos(r1,X,Y);
      -+pos(last,X,Y);

      // carry garbage to r2
      !take(garb,R);

      // goes back and continue to check
      !at(last);
      !check(slots).

+!take(S,L) : battery(B) & B > 0 & not charge
   <- !ensure_pick(S);
      !at(L);
      drop(S).

+!ensure_pick(S) : garbage(r1) & battery(L) & L > 0 & not charge
   <- pick(garb);
      !ensure_pick(S).
+!ensure_pick(_).

+!at(L) : pos(L, X, Y) & pos(r1, X, Y).
+!at(L) <- ?pos(L,X,Y);
           move_towards(0, X, Y);
           !at(L).
