# Task 1 - Implement an ALLIED agent that shows his position and distance to the flag and his distance to the base.
For this task we create a new file called `jasonAgent_ALLIED_T1.asl`, is a variation of the original `jasonAgent_ALLIED.asl` file. To accomplish the task, we edited the `get_agent_to_aim` plan in the new file, the plan is executed every time the agent looks for objects in his Field Of View  (*FOV*). Using the beliefs `?mi_position`, `?objective`, `?distance`, `?base_position`, etc. We calculate the distance to the flag and to the base, and we print agent's position.

## get_agent_to_aim:
```
?my_position(X, Y, Z);
.println("[TASK 1] My position: ", math.round(X),", ", math.round(Z));

?objective(FlagX, FlagY, FlagZ);
!distance(pos(FlagX, FlagY, FlagZ)); // Calculate de distance from agent's position to the flag
?distance(D);
.println("[TASK 1] Distance to the flag: ", math.round(D), " units.");

?base_position(BaseX, BaseY, BaseZ);
!distance(pos(BaseX, BaseY, BaseZ)); // Calculate de distance from agent's position to the base
?distance(Db);
.println("[TASK 1] Distance from the base: ", math.round(Db) , " units." );
```

# Task 2 - Implement a "crazy" AXIS agent that moves randomly.
First of all, we create a new file `jasonAgent_AXIS_T2.asl` for the new agent, and we initilized the `is_crazy(1)` and `rand_mov(1)` beliefs in the "init" plan.

As in the first task, we edited the `get_agent_to_aim` plan, but in this case we edited it in order to make the soldier move randomly. The random move is performed every 10 *ticks*, using the `rand_mov(N)` belief, using `N` as a counter. When `N` reaches 10, a random number is generated, using this number the soldier moves *up*, *down*, *left* or *right*, this is performed using the apropiate belief in each case: `order(up)`, `order(down)`, `order(left)` or `order(right)`.

Until we reach the 10 ticks to generate the random number, the tick counter is increased, removing `rand_mov(N)` and adding a new `rand_mov(N+1)`. We also removed all the `order(...)` beliefs.

## "Init" plan:
```
+!init
    <- 
    ?debug(Mode); if (Mode<=1) { .println("YOUR CODE FOR init GOES HERE.")};
    -+is_crazy(1);
    -+rand_mov(1).
```

## get_agent_to_aim:
```
+!get_agent_to_aim
    <-
    ...
    ?current_task(T);
    ?is_crazy(C);
    ?rand_mov(N);

    if(is_crazy(1) & (N mod 10) == 0){ // Tick reaches 10 and is crazy
        -rand_mov(N);
        +rand_mov(1);
        .random(X); // Generate the random number
        .println("[TASK 2] Moving randomly...");
        if(X < 1/4){
            -+order(up);  
        }else{
            if(X < 2/4){
                -+order(right);
            }else{
                if(X < 3/4){
                    -+order(down); 
                }else {
                    -+order(left);
                }
            }
        }
    }else{ // If the 10 ticks haven't passed yet... increase the counter
        -rand_mov(N);
        +rand_mov(N+1);
        ...
```

# Task 3 - Implement an AXIS agent that locates his "crazy" partner and follows him.