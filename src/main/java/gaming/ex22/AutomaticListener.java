package gaming.ex22;

public class AutomaticListener
//implements Animator.AnimatorListener 
{

    private final Runnable run;

    public AutomaticListener(Runnable run){
        this.run=run;
    }

//    @Override
//    public void onAnimationCancel(Animator animation) {
//        //DOES NOTHING
//    }
//
//    @Override
    public void onAnimationEnd(
//        Animator animation
    ) {
        run.run();
    }

//    @Override
//    public void onAnimationRepeat(Animator animation) {
//        //DOES NOTHING
//    }
//
//    @Override
//    public void onAnimationStart(Animator animation) {
//        //DOES NOTHING
//    }
}