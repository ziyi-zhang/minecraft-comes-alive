/*******************************************************************************
 * GuiSetup.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.client.gui;

import java.util.Random;

import mods.mca.core.MCA;
import mods.mca.core.io.WorldPropertiesManager;
import mods.mca.core.util.LanguageHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI shown to set up a new world with MCA.
 */
@SideOnly(Side.CLIENT)
public class GuiSetup extends AbstractGui
{
	private GuiButton genderButton;

	private GuiTextField nameTextField;
	private GuiButton hideTagsButton;
	private GuiButton autoGrowChildrenButton;
	private GuiButton displayMoodParticlesButton;
	private GuiButton preferenceButton;
	private GuiButton finishButton;

	private GuiButton backButton;
	private GuiButton nextButton;

	private boolean prefersMales = false;

	private boolean inNameSelectGui = false;
	private boolean inGenderSelectGui = false;
	private boolean inOptionsGui = false;

	/** Has this GUI been opened from a librarian? */
	private boolean viewedFromLibrarian = false;

	/** An instance of the player's world properties manager. */
	private WorldPropertiesManager manager;

	/**
	 * Constructor
	 * 
	 * @param 	player				The player that opened the GUI.
	 * @param 	viewedFromLibrarian	Was the GUI opened from a Librarian?
	 */
	public GuiSetup(EntityPlayer player, boolean viewedFromLibrarian)
	{
		super(player);
		this.viewedFromLibrarian = viewedFromLibrarian;
		this.manager = MCA.instance.playerWorldManagerMap.get(player.username);
	}

	@Override
	public void initGui()
	{
		controlList.clear();

		if (manager.worldProperties.playerGender.equals(""))
		{
			manager.worldProperties.playerGender = "Male";
		}

		if (manager.worldProperties.playerName.equals(""))
		{
			manager.worldProperties.playerName = player.username;
		}

		drawGenderSelectGui();
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (inNameSelectGui)
		{
			actionPerformedNameSelect(button);
		}

		else if (inGenderSelectGui)
		{
			actionPerformedGenderSelect(button);
		}

		else if (inOptionsGui)
		{
			actionPerformedOptions(button);
		}
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		if (inNameSelectGui)
		{
			nameTextField.updateCursorCounter();
		}
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		drawDefaultBackground();
		drawCenteredString(fontRenderer, LanguageHelper.getString("gui.title.setup"), width / 2, 20, 0xffffff);

		if (inGenderSelectGui)
		{
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.title.setup.gender"), width / 2, height / 2 - 80, 0xffffff);
			backButton.enabled = false;
		}

		else if (inNameSelectGui)
		{        	
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.title.setup.name"), width / 2, height / 2 - 80, 0xffffff);
			nameTextField.drawTextBox();
			backButton.enabled = true;
		}

		else if (inOptionsGui)
		{
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.title.setup.options"), width / 2, height / 2 - 80, 0xffffff);
			finishButton.enabled = true;
		}

		super.drawScreen(sizeX, sizeY, offset);
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		
		String input = prefersMales == true ? "Males" : "Females";

		try
		{
			String hashedPreference = MCA.getMD5Hash(input);
			int beginIndex = new Random().nextInt(5);
			int endIndex = beginIndex + new Random().nextInt(hashedPreference.length() - beginIndex);

			if (endIndex <= beginIndex || Math.abs(beginIndex - endIndex) < 5)
			{
				endIndex += 7;
			}

			manager.worldProperties.genderPreference = hashedPreference.substring(beginIndex, endIndex);
			manager.saveWorldProperties();
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
		}

		manager.saveWorldProperties();
	}

	@Override
	protected void keyTyped(char c, int i)
	{
		if (inNameSelectGui)
		{
			nameTextField.textboxKeyTyped(c, i);
			String text = nameTextField.getText().trim();
			manager.worldProperties.playerName = text;
		}
	}

	@Override
	protected void mouseClicked(int clickX, int clickY, int clicked)
	{
		super.mouseClicked(clickX, clickY, clicked);

		if (inNameSelectGui)
		{
			nameTextField.mouseClicked(clickX, clickY, clicked);
		}
	}

	/**
	 * Draws the gender selection GUI.
	 */
	private void drawGenderSelectGui()
	{
		inNameSelectGui   = false;
		inGenderSelectGui = true;
		inOptionsGui      = false;

		controlList.clear();

		controlList.add(genderButton = new GuiButton(1, width / 2 - 70, height / 2 - 10, 140, 20, LanguageHelper.getString("gui.button.setup.gender" + manager.worldProperties.playerGender.toLowerCase())));
		controlList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		controlList.add(nextButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.next")));

		genderButton.enabled = !viewedFromLibrarian;
		backButton.enabled = false;
	}

	/**
	 * Draws the name selection GUI.
	 */
	private void drawNameSelectGui()
	{
		Keyboard.enableRepeatEvents(true);

		inNameSelectGui   = true;
		inGenderSelectGui = false;
		inOptionsGui      = false;

		controlList.clear();

		nameTextField = new GuiTextField(fontRenderer, width / 2 - 100, height / 2 - 10, 200, 20);
		nameTextField.setText(manager.worldProperties.playerName);

		controlList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		controlList.add(nextButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.next")));

		backButton.enabled = false;

		nameTextField.setMaxStringLength(32);
	}

	/**
	 * Draws the options GUI.
	 */
	private void drawOptionsGui()
	{
		inNameSelectGui   = false;
		inGenderSelectGui = false;
		inOptionsGui      = true;

		controlList.clear();

		controlList.add(hideTagsButton              = new GuiButton(1, width / 2 - 80, height / 2 - 10, 170, 20, LanguageHelper.getString("gui.button.setup.hidesleepingtag")));
		controlList.add(autoGrowChildrenButton      = new GuiButton(2, width / 2 - 80, height / 2 + 10, 170, 20, LanguageHelper.getString("gui.button.setup.growchildrenautomatically")));
		controlList.add(displayMoodParticlesButton  = new GuiButton(3, width / 2 - 80, height / 2 + 30, 170, 20, LanguageHelper.getString("gui.button.setup.displaymoodparticles")));
		controlList.add(preferenceButton = new GuiButton(4, width / 2 - 80, height / 2 + 50, 170, 20, LanguageHelper.getString("gui.button.setup.preference")));

		controlList.add(backButton   = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		controlList.add(finishButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.setup.finish")));

		if (manager.worldProperties.hideSleepingTag) hideTagsButton.displayString = hideTagsButton.displayString + LanguageHelper.getString("gui.button.yes");
		else hideTagsButton.displayString = hideTagsButton.displayString + LanguageHelper.getString("gui.button.no");

		if (manager.worldProperties.childrenGrowAutomatically) autoGrowChildrenButton.displayString = autoGrowChildrenButton.displayString + LanguageHelper.getString("gui.button.yes");
		else autoGrowChildrenButton.displayString = autoGrowChildrenButton.displayString + LanguageHelper.getString("gui.button.no");

		if (prefersMales) preferenceButton.displayString = preferenceButton.displayString + LanguageHelper.getString("gui.button.setup.males");
		else preferenceButton.displayString = preferenceButton.displayString + LanguageHelper.getString("gui.button.setup.females");

		if (manager.worldProperties.displayMoodParticles) displayMoodParticlesButton.displayString = displayMoodParticlesButton.displayString + LanguageHelper.getString("gui.button.yes");
		else displayMoodParticlesButton.displayString = displayMoodParticlesButton.displayString + LanguageHelper.getString("gui.button.no");

		finishButton.enabled = false;

		if (MCA.instance.isDedicatedClient)
		{
			//overwriteTestificatesButton.enabled = false;
			autoGrowChildrenButton.enabled = false;
		}
	}

	/**
	 * Handles an action performed on the Gender Select GUI.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedGenderSelect(GuiButton button)
	{
		if (button == genderButton) 
		{
			if (manager.worldProperties.playerGender.equals("Male"))
			{
				manager.worldProperties.playerGender = "Female";
				prefersMales = true;
			}

			else
			{
				manager.worldProperties.playerGender = "Male";
				prefersMales = false;
			}

			manager.saveWorldProperties();
			drawGenderSelectGui();
		}

		else if (button == backButton)
		{
			return;
		}

		else if (button == nextButton)
		{
			drawNameSelectGui();
		}
	}

	/**
	 * Handles an action performed on the Name Select GUI.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedNameSelect(GuiButton button)
	{
		if (button == backButton)
		{
			drawGenderSelectGui();
		}

		else if (button == nextButton)
		{
			drawOptionsGui();
		}
	}

	/**
	 * Handles an action performed on the Options GUI.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedOptions(GuiButton button)
	{
		if (button == backButton)
		{
			drawNameSelectGui();
		}

		else if (button == finishButton)
		{
			manager.saveWorldProperties();	
			Minecraft.getMinecraft().displayGuiScreen(null);
		}

		else if (button == hideTagsButton)
		{
			manager.worldProperties.hideSleepingTag = !manager.worldProperties.hideSleepingTag;
			manager.saveWorldProperties();
			drawOptionsGui();
		}

		else if (button == autoGrowChildrenButton)
		{
			manager.worldProperties.childrenGrowAutomatically = !manager.worldProperties.childrenGrowAutomatically;
			manager.saveWorldProperties();
			drawOptionsGui();
		}

		else if (button == preferenceButton)
		{
			prefersMales = !prefersMales;
			drawOptionsGui();
		}

		else if (button == displayMoodParticlesButton)
		{
			manager.worldProperties.displayMoodParticles = !manager.worldProperties.displayMoodParticles;
			manager.saveWorldProperties();
			drawOptionsGui();
		}
	}
}
