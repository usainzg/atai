import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;

public class MarsEnv extends Environment {

    public static final int GSize = 7; // grid size
    public static final int GARB  = 16; // garbage code in grid model

    public static final Term    ns = Literal.parseLiteral("next(slot)");
    public static final Term    pg = Literal.parseLiteral("pick(garb)");
    public static final Term    dg = Literal.parseLiteral("drop(garb)");
    public static final Term    bg = Literal.parseLiteral("burn(garb)");
    public static final Literal g1 = Literal.parseLiteral("garbage(r1)");
    public static final Literal g2 = Literal.parseLiteral("garbage(r2)");
    public static final Literal g3 = Literal.parseLiteral("garbage(r3)");

    static Logger logger = Logger.getLogger(MarsEnv.class.getName());

    private MarsModel model;
    private MarsView  view;

    private Literal target = null;

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    @Override
    public void init(String[] args) {
        model = new MarsModel();
        view  = new MarsView(model);
        model.setView(view);
        updatePercepts();
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
            if (action.equals(ns)) {
                model.nextSlot();
            } else if (action.getFunctor().equals("move_towards")) {
                int id = (int)((NumberTerm)action.getTerm(0)).solve();
                int x = (int)((NumberTerm)action.getTerm(1)).solve();
                int y = (int)((NumberTerm)action.getTerm(2)).solve();
                model.moveTowards(id, x, y);
            } else if (action.getFunctor().equals("show_battery")) {
                model.showBattery((int)((NumberTerm) action.getTerm(0)).solve());
            } else if (action.getFunctor().equals("move_around")) {
                model.moveAround();
            } else if (action.getFunctor().equals("gen_garb")) {
                model.genGarb();
            } else if (action.equals(pg)) {
                model.pickGarb();
            } else if (action.equals(dg)) {
                model.dropGarb();
            } else if (action.equals(bg)) {
                model.burnGarb();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updatePercepts();

        try {
            Thread.sleep(400);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }

    /** creates the agents perception based on the MarsModel */
    void updatePercepts() {
        clearPercepts();

        Location r1Loc = model.getAgPos(0);
        Location r2Loc = model.getAgPos(1);
        Location r3Loc = model.getAgPos(2);
        Location r4Loc = model.getAgPos(3);

        Literal pos1 = Literal.parseLiteral("pos(r1," + r1Loc.x + "," + r1Loc.y + ")");
        Literal pos2 = Literal.parseLiteral("pos(r2," + r2Loc.x + "," + r2Loc.y + ")");
        Literal pos3 = Literal.parseLiteral("pos(r3," + r3Loc.x + "," + r3Loc.y + ")");
        Literal pos4 = Literal.parseLiteral("pos(r4," + r4Loc.x + "," + r4Loc.y + ")");

        addPercept(pos1);
        addPercept(pos2);
        addPercept(pos3);
        addPercept(pos4);

        if (model.hasObject(GARB, r1Loc)) {
            addPercept(g1);
        }
        if (model.hasObject(GARB, r2Loc)) {
            addPercept(g2);
        }
        if (model.hasObject(GARB, r3Loc)) {
            addPercept(g3);
        }
    }

    class MarsModel extends GridWorldModel {

        public static final int MErr = 2; // max error in pick/burn garb
        int nerr = 0, burnerr = 0, cont = 0; // number of tries of pick/burn garb
        boolean r1HasGarb = false; // whether r1 is carrying garbage or not
        int garbdrop;
        Random random = new Random(System.currentTimeMillis());
        int batteryLevel = 0;
        boolean showBattery = false;

        private MarsModel() {
            super(GSize, GSize, 4);
            garbdrop = random.nextInt(12);

            // initial location of agents
            try {
                // TASK 1 (c): randomly placing r1 agent
                setAgPos(0, random.nextInt(GSize), random.nextInt(GSize));

                // TASK 1 (b): randomly placing r2 agent
                Location r2Loc = new Location(random.nextInt(GSize), random.nextInt(GSize));
                setAgPos(1, r2Loc);
                setAgPos(2, random.nextInt(GSize), random.nextInt(GSize));
                
                setAgPos(3, getFreePos());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TASK 1 (a): initial RANDOM location of garbage
			for (int i = 0; i < Math.max(1, random.nextInt(GSize * GSize / 2)); i++) {
				int x = random.nextInt(GSize);
				int y = random.nextInt(GSize);  
				
				while (hasObject(GARB, x, y)) {
					x = random.nextInt(GSize);
					y = random.nextInt(GSize);
				}
				add(GARB, x, y);
			}
        }

        void nextSlot() throws Exception {
            Location r1 = getAgPos(0);
            // TASK 3 (a): move top-down 
            r1.y++;
            if (r1.y == getHeight()) {
                r1.x++;
                r1.y = 0;
            }
            // finished searching the whole grid
            // TASK 3 (b): move continously
            if (r1.x == getWidth()) {
                r1.x = 0;
                r1.y = 0;
            }
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
            setAgPos(2, getAgPos(2));
            setAgPos(3, getAgPos(3));
        }

        void moveTowards(int id, int x, int y) throws Exception {
            Location loc = getAgPos(id);
            if (loc.x < x)
                loc.x++;
            else if (loc.x > x)
                loc.x--;
            if (loc.y < y)
                loc.y++;
            else if (loc.y > y)
                loc.y--;
            
            for (int i = 0; i < 4; i++) {
                if (id == i) setAgPos(i, loc);
                else setAgPos(i, getAgPos(i));
            }
        }

        void pickGarb() {
            // r1 location has garbage
            if (model.hasObject(GARB, getAgPos(0)) && !getAgPos(0).equals(getAgPos(1))) { // TASK: Cannot be the same position
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARB, getAgPos(0));
                    nerr = 0;
                    r1HasGarb = true;
                } else {
                    nerr++;
                    System.out.println("[LOG] Failed picking garbage " + nerr + " times.");
                }
            }
        }

        void dropGarb() {
            if (r1HasGarb) {
                r1HasGarb = false;
                add(GARB, getAgPos(0));
            }
        }

        void burnGarb() {
            // r2 location has garbage
            if (model.hasObject(GARB, getAgPos(1))) {
                // TASK 2: r2 fail as r1 when picking up the garbage
                if (random.nextBoolean() || burnerr == MErr) {
                    remove(GARB, getAgPos(1));
                    burnerr = 0;
                } else {
                    burnerr += 1;
                    System.out.println("[LOG] Failed burning garbage " + burnerr + " times.");
                }
            }
        }

        void moveAround() {
            Location loc = getAgPos(2);
            int stepX = 0, stepY = 0;

            stepX = random.nextInt(2);
            if ((loc.x > 0 && random.nextBoolean()) || loc.x == GSize - 1) stepX *= -1;
            stepY = random.nextInt(2);
            if ((loc.y > 0 && random.nextBoolean()) || loc.y == GSize - 1) stepY *= -1;

            setAgPos(2, loc.x + stepX, loc.y + stepY);
            setAgPos(0, getAgPos(0));
            setAgPos(1, getAgPos(1));
            setAgPos(3, getAgPos(3));
        }

        void genGarb() {
            Location loc = getAgPos(2);
            if (!model.hasObject(GARB, loc) && random.nextFloat() < 0.1) add(GARB, loc);
        }

        void showBattery(int level) {
            batteryLevel = level;
            showBattery = true;
        }
    }

    class MarsView extends GridWorldView {

        public MarsView(MarsModel model) {
            super(model, "Mars World", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
            case MarsEnv.GARB:
                drawGarb(g, x, y);
                break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "R"+(id+1);
            if (id == 3) label = "Charger";
            c = Color.blue;
            if (id == 0) {
                c = Color.yellow;
                if (((MarsModel)model).r1HasGarb) {
                    label += " - G";
                    c = Color.orange;
                // SHOW THE BATTERY LEVEL!
                } else if (((MarsModel)model).showBattery) {
                    int level = ((MarsModel)model).batteryLevel;
                    label += "-" + Integer.toString(level) + "%";
                    if (level < 10) {
                        c = Color.red;
                    } else if (level < 30) {
                        c = Color.orange;
                    }
                }
            } else if (id == 2) {
                c = Color.green;
            } else if (id == 3) {
                c = Color.pink;
            }
            super.drawAgent(g, x, y, c, -1);
            if (id == 0 || id == 2) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.white);
            }
            super.drawString(g, x, y, defaultFont, label);
            repaint();
        }

        public void drawGarb(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.white);
            drawString(g, x, y, defaultFont, "G");
        }
    }
}
