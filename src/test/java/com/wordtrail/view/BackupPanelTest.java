package com.wordtrail.view;
import com.wordtrail.app.Application;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(20)
class BackupPanelTest {
    @TempDir Path directory;
    @Test void exportCancelValidateConfirmAndRestoreUseTheRealControllerAndStore() throws Exception {
        try(Application app=new Application(directory.resolve("profile"))) {
            app.controller().setup("allen","password123".toCharArray());
            app.controller().study().goal(77); app.controller().study().start();
            JPanel root=new JPanel(new BorderLayout());
            AtomicReference<String> message=new AtomicReference<>("");
            UiTasks tasks=new UiTasks(root,message::set);
            AtomicReference<String> selected=new AtomicReference<>();
            AtomicBoolean signedOut=new AtomicBoolean();
            BackupPanel panel=edt(()->new BackupPanel(app.controller(),tasks,()->signedOut.set(true),export->selected.get()));
            edt(()->{ root.add(panel); root.setSize(934,660); layout(root); return null; });
            click(root,"backup.export"); assertTrue(message.get().contains("cancelled"));
            Path backup=directory.resolve("learning.json"); selected.set(backup.toString());
            click(root,"backup.export"); idle(tasks); assertTrue(Files.exists(backup));
            app.controller().study().goal(99);
            click(root,"backup.inspect"); idle(tasks);
            assertFalse(edt(()->((JButton)find(root,"backup.restore")).isEnabled()));
            assertTrue(edt(()->((JTextArea)find(root,"backup.details")).getText()).contains("allen"));
            edt(()->{ ((JCheckBox)find(root,"backup.confirm")).doClick(); return null; });
            assertTrue(edt(()->((JButton)find(root,"backup.restore")).isEnabled()));
            edt(()->{
                layout(root);
                BufferedImage image=new BufferedImage(934,660,BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics=image.createGraphics(); root.printAll(graphics); graphics.dispose();
                Path path=Path.of(System.getProperty("wordtrail.uiSnapshots","target/ui-snapshots"));
                Files.createDirectories(path); javax.imageio.ImageIO.write(image,"png",path.resolve("backup-restore.png").toFile());
                return null;
            });
            click(root,"backup.restore"); idle(tasks); assertTrue(signedOut.get());
            assertTrue(message.get().contains("Recovery copy"));
            app.controller().login("allen","password123".toCharArray());
            assertEquals(77,app.controller().study().show().goal);
        }
    }
    private static void click(Container root,String name) throws Exception {
        edt(()->{ ((JButton)find(root,name)).doClick(); return null; });
    }
    private static Component find(Component component,String name) {
        if(name.equals(component.getName())) { return component; }
        if(component instanceof Container) {
            for(Component child:((Container)component).getComponents()) {
                Component match=find(child,name); if(match!=null) { return match; }
            }
        }
        return null;
    }
    private static void layout(Container component) {
        component.doLayout();
        for(Component child:component.getComponents()) { if(child instanceof Container) { layout((Container)child); } }
    }
    private static void idle(UiTasks tasks) throws Exception {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
        while(tasks.isBusy() && System.nanoTime()<deadline) { Thread.sleep(10); }
        edt(()->null); assertFalse(tasks.isBusy());
    }
    private static <T> T edt(Callable<T> work) throws Exception {
        FutureTask<T> task=new FutureTask<>(work); SwingUtilities.invokeAndWait(task); return task.get();
    }
}
