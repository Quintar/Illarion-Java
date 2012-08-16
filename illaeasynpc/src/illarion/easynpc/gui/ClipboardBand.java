/*
 * This file is part of the Illarion easyNPC Editor.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion easyNPC Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion easyNPC Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion easyNPC Editor.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.easynpc.gui;

import illarion.easynpc.Lang;
import org.bushe.swing.event.EventBusAction;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a band for the ribbon bar at the top of the editor. This band allows
 * to access and use the clip board.
 *
 * @author Martin Karing
 */
final class ClipboardBand extends JRibbonBand {
    /**
     * The serialization UID of this ribbon band.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The constructor for this clipboard band that prepares the buttons and all
     * settings needed for the proper display.
     */
    @SuppressWarnings("nls")
    public ClipboardBand() {
        super(Lang.getMsg(ClipboardBand.class, "title"), null);

        final JCommandButton pasteButton =
                new JCommandButton(Lang.getMsg(getClass(), "pasteButton"),
                        Utils.getResizableIconFromResource("editpaste.png"));
        final JCommandButton copyButton =
                new JCommandButton(Lang.getMsg(getClass(), "copyButton"),
                        Utils.getResizableIconFromResource("editcopy.png"));
        final JCommandButton cutButton =
                new JCommandButton(Lang.getMsg(getClass(), "cutButton"),
                        Utils.getResizableIconFromResource("editcut.png"));

        pasteButton.setActionRichTooltip(new RichTooltip(Lang.getMsg(
                getClass(), "pasteButtonTooltipTitle"), Lang.getMsg(getClass(),
                "pasteButtonTooltip")));
        copyButton.setActionRichTooltip(new RichTooltip(Lang.getMsg(
                getClass(), "copyButtonTooltipTitle"), Lang.getMsg(getClass(),
                "copyButtonTooltip")));
        cutButton.setActionRichTooltip(new RichTooltip(Lang.getMsg(getClass(),
                "cutButtonTooltipTitle"), Lang.getMsg(getClass(),
                "cutButtonTooltip")));

        pasteButton.getActionModel().setActionCommand("paste");
        copyButton.getActionModel().setActionCommand("copy");
        cutButton.getActionModel().setActionCommand("cut");

        pasteButton.addActionListener(new EventBusAction());
        copyButton.addActionListener(new EventBusAction());
        cutButton.addActionListener(new EventBusAction());

        addCommandButton(pasteButton, RibbonElementPriority.TOP);
        addCommandButton(copyButton, RibbonElementPriority.MEDIUM);
        addCommandButton(cutButton, RibbonElementPriority.MEDIUM);

        final List<RibbonBandResizePolicy> policies =
                new ArrayList<RibbonBandResizePolicy>();
        policies.add(new CoreRibbonResizePolicies.Mirror(getControlPanel()));
        policies.add(new CoreRibbonResizePolicies.Mid2Low(getControlPanel()));
        setResizePolicies(policies);
    }
}
