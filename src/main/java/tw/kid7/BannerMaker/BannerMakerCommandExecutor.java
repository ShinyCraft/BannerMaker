package tw.kid7.BannerMaker;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import tw.kid7.BannerMaker.command.BannerMakerCommand;
import tw.kid7.BannerMaker.command.AbstractCommand;

public class BannerMakerCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        AbstractCommand cmd = new BannerMakerCommand(sender);
        cmd.execute(sender, command, label, args);
        return true;
    }
}