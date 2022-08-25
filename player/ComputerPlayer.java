package player;
import mnkgame.*;

public class ComputerPlayer implements MNKPlayer{
    private int rows, cols, align;
    private boolean first;
    private int timeout;
    private mnkTree mnkTree;

    public  ComputerPlayer(){
    }

    public void initPlayer(int m, int n, int k, boolean first, int timeout){
        this.rows = m;
        this.cols = n;
        this.align = k;
        this.first = first;
        this.mnkTree = new mnkTree(m, n, k, first);
    }

    public MNKCell selectCell(MNKCell[] MC, MNKCell[] FC){

    }

    public String playerName(){
        return "MrRobot";
    }

}




