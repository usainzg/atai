# Task 1 - Implement an ALLIED agent that shows his position and distance to the flag and his distance to the base.
For this task we create a new file called `jasonAgent_ALLIED_T1.asl`, is a variation of the original `jasonAgent_ALLIED.asl` file. To accomplish the task, we edited the `get_agent_to_aim` plan in the new file, the plan is executed every time the agent looks for objects in his Field Of View  (*FOV*). Using the beliefs `?mi_position`, `?objective`, `?distance`, `?base_position`, etc. We calculate the distance to the flag and to the base, and we print agent's position.

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
