package beast.app.beauti;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.JTextField;

import beast.app.draw.InputEditor;
import beast.app.draw.ListInputEditor;
import beast.app.draw.PluginPanel;
import beast.core.Input;
import beast.core.Plugin;
import beast.evolution.likelihood.TreeLikelihood;

public class TreeLikelihoodListInputEditor extends ListInputEditor {
	private static final long serialVersionUID = 1L;

	@Override
	public Class<?> type() {
		return List.class;
	}
	@Override
	public Class<?> baseType() {
		return TreeLikelihood.class;
	}
	
	@Override
    public void init(Input<?> input, Plugin plugin, EXPAND bExpand, boolean bAddButtons) {
		super.init(input, plugin, bExpand, bAddButtons);
		add(Box.createVerticalGlue());
	}

	@Override
    protected void addPluginItem(Box itemBox, Plugin plugin) {
    	TreeLikelihood likelihood = (TreeLikelihood) plugin;
        // Label of name
        String sName = plugin.getID();
        if (sName == null || sName.length() == 0) {
            sName = plugin.getClass().getName();
            sName = sName.substring(sName.lastIndexOf('.') + 1);
        }
        JTextField entry= new JTextField(sName);
        entry.setMinimumSize(new Dimension(200, 16));
        entry.setMaximumSize(new Dimension(200, 20));
        m_entries.add(entry);
        itemBox.add(entry);
        
        itemBox.add(Box.createHorizontalGlue());
        try {
        	InputEditor editor = PluginPanel.createInputEditor(likelihood.m_tree, likelihood, false, EXPAND.FALSE, BUTTONSTATUS.ALL, this);
        	editor.addValidationListener(this);
        	editor.setMaximumSize(new Dimension(100,30));
	        itemBox.add(editor);
	        editor = PluginPanel.createInputEditor(likelihood.m_pSiteModel, likelihood, false, EXPAND.FALSE, BUTTONSTATUS.ALL, this);
        	editor.addValidationListener(this);
        	editor.setMaximumSize(new Dimension(100,30));
	        itemBox.add(editor);
	        editor = PluginPanel.createInputEditor(likelihood.m_pBranchRateModel, likelihood, false, EXPAND.FALSE, BUTTONSTATUS.ALL, this);
        	editor.addValidationListener(this);
        	editor.setMaximumSize(new Dimension(100,30));
	        itemBox.add(editor);
        } catch (Exception e) {
			e.printStackTrace();
		} 
    } // addPluginItem

} // class TreeLikelihoodListInputEditor
