package wtf.nebula.impl.wdl;

public class WDLSaveAsync implements Runnable
{
    public void run()
    {
        WDL.saveEverything();
        WDL.saving = false;
        WDL.onSaveComplete();
    }
}
