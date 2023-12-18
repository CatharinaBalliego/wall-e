package discord.walle;

import java.util.Arrays;
import java.util.EnumSet;

import discord.walle.commands.BulkDeleteMessage;
import net.dv8tion.jda.api.JDA;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class BotMain extends ListenerAdapter{

	
	public static void main(String[] args) throws InterruptedException {

		
		
		// TODO Auto-generated method stub
		JDA bot = JDABuilder.createDefault(Token.token)
				.addEventListeners(new BotMain())
				.setActivity(Activity.customStatus("cleaning"))
				//.addEventListeners(new BulkDeleteMessage())
				//.setBulkDeleteSplittingEnabled(true)
				.build().awaitReady();
		
		CommandListUpdateAction commands = bot.updateCommands();
		
//		bot.updateCommands().addCommands(Commands.slash("ping", "Calculate ping of the bot")).queue();;
		
		commands.addCommands(Commands.slash("ping", "Calculate ping of the bot"));
		
		
		
		 commands.addCommands(
		            Commands.slash("prune", "Prune messages")
		                .addOption(OptionType.INTEGER, "amount", "How many messages to prune (Default 100)") // simple optional argument
		               // .setGuildOnly(true)
		                //.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
		        );
		
		commands.queue();
		
		
	}

	@Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		//event.deferReply().queue();
		switch(event.getName()) {
			case "ping":
				long time = System.currentTimeMillis();
				event.reply("Pong:  " + time).queue();
				break;
			case "delete":
				//int messages = event.getOption("amount").getAsInt();
				//Channel channel = event.getChannel();
				//BulkDeleteMessage.bulkDelete(messages, channel);
				break;
			case "prune":
				prune(event);
				break;
			default:
                System.out.printf("Unknown command %s used by %#s %n", event.getName(), event.getUser());
		}
	}
	
	@Override
    public void onButtonInteraction(ButtonInteractionEvent event)
    {
        String[] id = event.getComponentId().split(":"); // this is the custom id we specified in our button
        String authorId = id[0];
        String type = id[1];
        // Check that the button is for the user that clicked it, otherwise just ignore the event (let interaction fail)
        if (!authorId.equals(event.getUser().getId()))
            return;
        event.deferEdit().queue(); // acknowledge the button was clicked, otherwise the interaction will fail
 
        MessageChannel channel = event.getChannel();
        switch (type)
        {
            case "prune":
                int amount = Integer.parseInt(id[2]);
                System.out.println(amount);
                event.getChannel().getIterableHistory()
                    .skipTo(event.getMessageIdLong())
                    .takeAsync(amount)
                    .thenAccept(channel::purgeMessages);
                // fallthrough delete the prompt message with our buttons
            case "delete":
                event.getHook().deleteOriginal().queue();
        }
    }

	
	 public void prune(SlashCommandInteractionEvent event)
	    {
	        OptionMapping amountOption = event.getOption("amount"); // This is configured to be optional so check for null
	        int amount = amountOption == null
	                ? 100 // default 100
	                : (int) Math.min(200, Math.max(2, amountOption.getAsLong())); // enforcement: must be between 2-200
	        String userId = event.getUser().getId();
	        event.reply("This will delete " + amount + " messages.\nAre you sure?") // prompt the user with a button menu
	            .addActionRow(// this means "<style>(<id>, <label>)", you can encode anything you want in the id (up to 100 characters)
	                Button.secondary(userId + ":delete", "Nevermind!"),
	                Button.danger(userId + ":prune:" + amount, "Yes!")) // the first parameter is the component id we use in onButtonInteraction above
	            .queue();
	    }
	
	
	
}
