package com.rexcantor64.triton.commands;

import com.rexcantor64.triton.Triton;
import com.rexcantor64.triton.api.language.Language;
import com.rexcantor64.triton.commands.handler.Command;
import com.rexcantor64.triton.commands.handler.CommandEvent;
import lombok.val;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SetLanguageCommand implements Command {

    @Override
    public boolean handleCommand(CommandEvent event) {
        val sender = event.getSender();
        val args = event.getArgs();

        sender.assertPermission("triton.setlanguage", "multilanguageplugin.setlanguage");

        if (args.length == 0) {
            sender.sendMessageFormatted("help.setlanguage", event.getLabel());
            return true;
        }

        UUID target;
        val langName = args[0];

        if (args.length >= 2) {
            sender.assertPermission("triton.setlanguage.others", "multilanguageplugin.setlanguage.others");

            target = Triton.get().getPlayerUUIDFromString(args[1]);

            if (target == null) {
                sender.sendMessageFormatted("error.player-not-found-use-uuid", args[1]);
                return true;
            }
        } else if (sender.getUUID() != null) {
            target = sender.getUUID();
        } else {
            sender.sendMessage("Only players");
            return true;
        }

        val lang = Triton.get().getLanguageManager().getLanguageByName(langName, false);
        if (lang == null) {
            sender.sendMessageFormatted("error.lang-not-found", langName);
            return true;
        }

        if (Triton.get().getPlayerManager().hasPlayer(target))
            Triton.get().getPlayerManager().get(target).setLang(lang);
        else {
            if (event.getEnvironment() == CommandEvent.Environment.SPIGOT && Triton.get().getConfig().isBungeecord()) {
                sender.sendMessage("Changing the language of offline players must be done through the proxy " +
                        "console.");
                return true;
            }
            Triton.get().getStorage().setLanguage(target, null, lang);
        }

        if (target.equals(sender.getUUID()))
            sender.sendMessageFormatted("success.setlanguage", lang.getDisplayName());
        else
            sender.sendMessageFormatted("success.setlanguage-others", args[1], lang.getDisplayName());
        return true;
    }

    @Override
    public List<String> handleTabCompletion(CommandEvent event) {
        val sender = event.getSender();
        sender.assertPermission("triton.setlanguage", "multilanguageplugin.setlanguage");

        // returning "null" triggers the player list
        if (event.getArgs().length != 1) return null;

        return Triton.get().getLanguageManager().getAllLanguages().stream().map(Language::getName)
                .filter(name -> name.toLowerCase().startsWith(event.getArgs()[0])).collect(Collectors.toList());
    }
}
