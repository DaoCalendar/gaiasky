package gaia.cu9.ari.gaiaorbit.gui.swing;

import gaia.cu9.ari.gaiaorbit.GaiaSandboxDesktop;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.gui.swing.callback.Callback;
import gaia.cu9.ari.gaiaorbit.gui.swing.callback.CallbackTask;
import gaia.cu9.ari.gaiaorbit.gui.swing.jsplash.GuiUtility;
import gaia.cu9.ari.gaiaorbit.gui.swing.jsplash.JSplashLabel;
import gaia.cu9.ari.gaiaorbit.gui.swing.version.VersionChecker;
import gaia.cu9.ari.gaiaorbit.interfce.KeyMappings;
import gaia.cu9.ari.gaiaorbit.interfce.KeyMappings.ProgramAction;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import com.alee.extended.filechooser.WebDirectoryChooser;
import com.alee.laf.button.WebButton;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.utils.FileUtils;
import com.alee.utils.swing.DialogOptions;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.JsonValue;

/**
 * The configuration dialog to set the resolution, the screen mode, etc.
 * @author Toni Sagrista
 *
 */
public class ConfigDialog extends I18nJFrame {
    private static long fiveDaysMs = 5 * 24 * 60 * 60 * 1000;

    /** Border params **/
    private static final Color bcol = new Color(0.0f, 0.0f, 0.0f);
    private static final int just = TitledBorder.LEADING;
    private static final int pos = TitledBorder.ABOVE_TOP;
    private static final int thick = 2;

    JFrame frame;
    JLabel checkLabel;
    JPanel checkPanel;
    Color darkgreen, darkred, transparent;
    JButton cancelButton, okButton;
    String vislistdata;
    JTree visualisationsTree;

    public ConfigDialog(final GaiaSandboxDesktop gsd, boolean startup) {
	super(startup ? GlobalConf.getFullApplicationName() : txt("gui.settings"));
	initialize(gsd, startup);

	if (startup) {
	    /** SPLASH IMAGE **/
	    URL url = this.getClass().getResource("/img/splash/splash-s.jpg");
	    JSplashLabel label = new JSplashLabel(url, txt("gui.build", GlobalConf.version.build) + " - " + txt("gui.version", GlobalConf.version.version), null, Color.lightGray);
	    JPanel imagePanel = new JPanel(new GridLayout(1, 1, 0, 0));
	    imagePanel.add(label);
	    imagePanel.setBackground(Color.black);
	    frame.add(imagePanel, BorderLayout.NORTH);
	}

	frame.pack();
	GuiUtility.centerOnScreen(frame);
	frame.setVisible(true);
	frame.setEnabled(true);
	frame.setAutoRequestFocus(true);

	// ESC closes the frame
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		KeyStroke.getKeyStroke("ESCAPE"), "closeTheDialog");
	getRootPane().getActionMap().put("closeTheDialog",
		new AbstractAction() {

		    private static final long serialVersionUID = 8360999630557775801L;

		    @Override
		    public void actionPerformed(ActionEvent e) {
			//This should be replaced by the action you want to perform
			cancelButton.doClick();
		    }
		});

	// Request focus
	frame.getRootPane().setDefaultButton(okButton);
	okButton.requestFocus();
    }

    private void initialize(final GaiaSandboxDesktop gsd, final boolean startup) {
	frame = this;
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frame.setResizable(false);

	darkgreen = new Color(0, .5f, 0);
	darkred = new Color(.8f, 0, 0);
	transparent = new Color(0f, 0f, 0f, 0f);

	// Build content
	frame.setLayout(new BorderLayout(0, 0));

	/** BODY **/
	JPanel body = new JPanel(new MigLayout("", "[grow,fill][]", ""));

	/** VERSION CHECK **/
	checkPanel = new JPanel(new MigLayout("", "[][]", "[]4[]"));
	checkLabel = new JLabel("");
	checkPanel.add(checkLabel);
	if (GlobalConf.program.LAST_CHECKED == null || GlobalConf.program.LAST_VERSION.isEmpty() || new Date().getTime() - GlobalConf.program.LAST_CHECKED.getTime() > fiveDaysMs) {
	    // Check!
	    checkLabel.setText(txt("gui.newversion.checking"));
	    getCheckVersionThread().start();
	} else {
	    // Inform latest
	    newVersionCheck(GlobalConf.program.LAST_VERSION);

	}

	/** TABBED PANEL **/

	final JXTabbedPane tabbedPane = new JXTabbedPane(JTabbedPane.LEFT);
	AbstractTabRenderer renderer = (AbstractTabRenderer) tabbedPane.getTabRenderer();
	renderer.setPrototypeText("123456789012345678");
	renderer.setHorizontalTextAlignment(SwingConstants.LEADING);

	/**
	 * ====== GRAPHICS TAB =======
	 */

	/** RESOLUTION **/
	JPanel mode = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill]", ""));
	mode.setBorder(new TitledBorder(new MatteBorder(new Insets(thick, 0, 0, 0), bcol), txt("gui.resolutionmode"), just, pos));

	// Full screen mode resolutions
	DisplayMode[] modes = LwjglApplicationConfiguration.getDisplayModes();
	final JComboBox<DisplayMode> fullScreenResolutions = new JComboBox<DisplayMode>(modes);

	DisplayMode selectedMode = null;
	for (DisplayMode dm : modes) {
	    if (dm.width == GlobalConf.screen.FULLSCREEN_WIDTH && dm.height == GlobalConf.screen.FULLSCREEN_HEIGHT) {
		selectedMode = dm;
		break;
	    }
	}
	if (selectedMode != null)
	    fullScreenResolutions.setSelectedItem(selectedMode);

	// Get current resolution
	DisplayMode nativeMode = LwjglApplicationConfiguration.getDesktopDisplayMode();

	// Windowed mode resolutions
	JPanel windowedResolutions = new JPanel(new MigLayout("", "[][grow,fill][][grow,fill]", "[][]4[][]"));
	final JSpinner widthField = new JSpinner(new SpinnerNumberModel(MathUtils.clamp(GlobalConf.screen.SCREEN_WIDTH, 100, nativeMode.width), 100, nativeMode.width, 1));
	final JSpinner heightField = new JSpinner(new SpinnerNumberModel(MathUtils.clamp(GlobalConf.screen.SCREEN_HEIGHT, 100, nativeMode.height), 100, nativeMode.height, 1));
	final JCheckBox resizable = new JCheckBox("Resizable", GlobalConf.screen.RESIZABLE);

	windowedResolutions.add(new JLabel(txt("gui.width") + ":"));
	windowedResolutions.add(widthField);
	windowedResolutions.add(new JLabel(txt("gui.height") + ":"));
	windowedResolutions.add(heightField, "wrap");
	windowedResolutions.add(resizable, "span");

	// Radio buttons 
	final JRadioButton fullscreen = new JRadioButton(txt("gui.fullscreen"));
	fullscreen.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		GlobalConf.screen.FULLSCREEN = fullscreen.isSelected();
		selectFullscreen(fullscreen.isSelected(), widthField, heightField, fullScreenResolutions, resizable);
	    }
	});
	fullscreen.setSelected(GlobalConf.screen.FULLSCREEN);

	final JRadioButton windowed = new JRadioButton(txt("gui.windowed"));
	windowed.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		GlobalConf.screen.FULLSCREEN = !windowed.isSelected();
		selectFullscreen(!windowed.isSelected(), widthField, heightField, fullScreenResolutions, resizable);
	    }
	});
	windowed.setSelected(!GlobalConf.screen.FULLSCREEN);
	selectFullscreen(GlobalConf.screen.FULLSCREEN, widthField, heightField, fullScreenResolutions, resizable);

	ButtonGroup modeButtons = new ButtonGroup();
	modeButtons.add(fullscreen);
	modeButtons.add(windowed);

	mode.add(fullscreen);
	mode.add(fullScreenResolutions, "wrap");
	mode.add(windowed);
	mode.add(windowedResolutions);

	/** GRAPHICS **/
	JPanel graphics = new JPanel(new MigLayout("", "[][]", ""));
	graphics.setBorder(new TitledBorder(new MatteBorder(new Insets(thick, 0, 0, 0), bcol), txt("gui.graphicssettings"), just, pos));

	// AA
	JTextArea msaaInfo = new JTextArea(txt("gui.aa.info")) {
	    @Override
	    public void setBorder(Border border) {
		// No!
	    }
	};
	msaaInfo.setBackground(transparent);
	msaaInfo.setForeground(darkgreen);
	msaaInfo.setEditable(false);

	ThreadComboBoxBean[] aas = new ThreadComboBoxBean[] { new ThreadComboBoxBean(txt("gui.aa.no"), 0), new ThreadComboBoxBean(txt("gui.aa.fxaa"), -1), new ThreadComboBoxBean(txt("gui.aa.nfaa"), -2), new ThreadComboBoxBean(txt("gui.aa.msaa", 2), 2), new ThreadComboBoxBean(txt("gui.aa.msaa", 4), 4), new ThreadComboBoxBean(txt("gui.aa.msaa", 8), 8), new ThreadComboBoxBean(txt("gui.aa.msaa", 16), 16) };
	final JComboBox<ThreadComboBoxBean> msaa = new JComboBox<ThreadComboBoxBean>(aas);
	msaa.setSelectedItem(aas[idxAa(2, GlobalConf.postprocess.POSTPROCESS_ANTIALIAS)]);

	// Vsync
	final JCheckBox vsync = new JCheckBox(txt("gui.vsync"), GlobalConf.screen.VSYNC);

	// Pixel renderer
	ThreadComboBoxBean[] pixelRenderers = new ThreadComboBoxBean[] { new ThreadComboBoxBean(txt("gui.pixrenderer.normal"), 0), new ThreadComboBoxBean(txt("gui.pixrenderer.bloom"), 1) };
	final JComboBox<ThreadComboBoxBean> pixelRenderer = new JComboBox<ThreadComboBoxBean>(pixelRenderers);
	pixelRenderer.setSelectedItem(pixelRenderers[GlobalConf.scene.PIXEL_RENDERER]);

	graphics.add(msaaInfo, "span,wrap");
	graphics.add(new JLabel(txt("gui.aa") + ":"));
	graphics.add(msaa);
	graphics.add(vsync, "wrap");
	graphics.add(new JLabel(txt("gui.pixrenderer") + ":"));
	graphics.add(pixelRenderer, "span");

	/** NOTICE **/
	JPanel notice = new JPanel(new MigLayout("", "[]", ""));
	JLabel noticeText = new JLabel(txt("gui.graphics.info"));
	noticeText.setForeground(darkgreen);
	notice.add(noticeText);

	/** SUB TABBED PANE **/
//	JTabbedPane graphicsTabbedPane = new JTabbedPane();
//	graphicsTabbedPane.setTabPlacement(JTabbedPane.TOP);
//
//	graphicsTabbedPane.addTab(txt("gui.resolutionmode"), mode);
//	graphicsTabbedPane.addTab(txt("gui.graphicssettings"), graphics);

	JPanel graphicsPanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
	//graphicsPanel.add(graphicsTabbedPane, "wrap");
	graphicsPanel.add(mode, "wrap");
	graphicsPanel.add(graphics, "wrap");
	if (!startup) {
	    graphicsPanel.add(notice, "wrap");
	}

	tabbedPane.addTab(txt("gui.graphics"), IconManager.get("config/graphics"), graphicsPanel);
	tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

	/**
	 * ====== USER INTERFACE TAB =======
	 */
	JPanel ui = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
	ui.setBorder(new TitledBorder(new MatteBorder(new Insets(thick, 0, 0, 0), bcol), txt("gui.ui.interfacesettings"), just, pos));

	File i18nfolder = new File("./data/i18n/");
	if (!i18nfolder.exists()) {
	    i18nfolder = new File("../android/assets/i18n/");
	}
	String i18nname = "gsbundle";
	String[] files = i18nfolder.list();
	LangComboBoxBean[] langs = new LangComboBoxBean[files.length];
	int i = 0;
	for (String file : files) {
	    if (file.startsWith("gsbundle") && file.endsWith(".properties")) {
		String locale = file.substring(i18nname.length(), file.length() - ".properties".length());
		if (locale.length() != 0) {
		    // Remove underscore _
		    locale = locale.substring(1).replace("_", "-");
		    Locale loc = Locale.forLanguageTag(locale);
		    langs[i] = new LangComboBoxBean(loc);
		} else {
		    langs[i] = new LangComboBoxBean(I18n.bundle.getLocale());
		}
	    }
	    i++;
	}
	Arrays.sort(langs);
	final JComboBox<LangComboBoxBean> lang = new JComboBox<LangComboBoxBean>(langs);
	lang.setSelectedItem(langs[idxLang(GlobalConf.program.LOCALE, langs)]);

	String[] themes = new String[] { "dark", "bright", "dark-big" };
	final JComboBox<String> theme = new JComboBox<String>(themes);
	theme.setSelectedItem(GlobalConf.program.UI_THEME);

	ui.add(new JLabel(txt("gui.ui.language") + ":"));
	ui.add(lang, "wrap");
	ui.add(new JLabel(txt("gui.ui.theme") + ":"));
	ui.add(theme);

	/** NOTICE **/
	JPanel uiNotice = new JPanel(new MigLayout("", "[]", ""));
	JLabel uinoticeText = new JLabel(txt("gui.ui.info"));
	uinoticeText.setForeground(darkgreen);
	uiNotice.add(uinoticeText);

	JPanel uiPanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
	uiPanel.add(ui, "wrap");
	if (!startup) {
	    uiPanel.add(uiNotice, "wrap");
	}

	tabbedPane.addTab(txt("gui.ui.interface"), IconManager.get("config/interface"), uiPanel);
	tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

	/**
	 * ====== PERFORMANCE TAB =======
	 */

	/** MULTITHREAD **/
	JPanel multithread = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
	multithread.setBorder(new TitledBorder(new MatteBorder(new Insets(thick, 0, 0, 0), bcol), txt("gui.multithreading"), just, pos));

	int maxthreads = Runtime.getRuntime().availableProcessors();
	ThreadComboBoxBean[] cbs = new ThreadComboBoxBean[maxthreads + 1];
	cbs[0] = new ThreadComboBoxBean(txt("gui.letdecide"), 0);
	for (i = 1; i <= maxthreads; i++) {
	    cbs[i] = new ThreadComboBoxBean(txt("gui.thread", i), i);
	}
	final JComboBox<ThreadComboBoxBean> numThreads = new JComboBox<ThreadComboBoxBean>(cbs);
	numThreads.setSelectedIndex(GlobalConf.performance.NUMBER_THREADS);

	final JCheckBox multithreadCb = new JCheckBox(txt("gui.thread.enable"));
	multithreadCb.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		numThreads.setEnabled(multithreadCb.isSelected());
	    }
	});
	multithreadCb.setSelected(GlobalConf.performance.MULTITHREADING);
	numThreads.setEnabled(multithreadCb.isSelected());

	multithread.add(multithreadCb, "span");
	multithread.add(new JLabel(txt("gui.thread.number") + ":"));
	multithread.add(numThreads);

	JPanel performancePanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	performancePanel.add(multithread, "wrap");

	tabbedPane.addTab(txt("gui.performance"), IconManager.get("config/performance"), performancePanel);
	tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

	/**
	 * ====== CONTROLS TAB =======
	 */
	JPanel controls = new JPanel(new MigLayout("", "[grow,fill][]", ""));
	controls.setBorder(new TitledBorder(new MatteBorder(new Insets(thick, 0, 0, 0), bcol), txt("gui.keymappings"), just, pos));

	Map<TreeSet<Integer>, ProgramAction> maps = KeyMappings.instance.mappings;
	Set<TreeSet<Integer>> keymaps = maps.keySet();

	String[] headers = new String[] { txt("gui.keymappings.action"), txt("gui.keymappings.keys") };
	String[][] data = new String[maps.size()][2];
	i = 0;
	for (TreeSet<Integer> keys : keymaps) {
	    ProgramAction action = maps.get(keys);
	    data[i][0] = action.actionName;
	    data[i][1] = keysToString(keys);
	    i++;
	}

	JTable table = new JTable(data, headers);
	table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	table.setRowSelectionAllowed(true);
	table.setColumnSelectionAllowed(false);

	JScrollPane controlsScrollPane = new JScrollPane(table);
	controlsScrollPane.setPreferredSize(new Dimension(0, 180));

	JLabel lab = new JLabel(txt("gui.noteditable"));
	lab.setForeground(darkred);
	controls.add(lab, "wrap");
	controls.add(controlsScrollPane, "wrap");

	JPanel controlsPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	controlsPanel.add(controls, "wrap");

	tabbedPane.addTab(txt("gui.controls"), IconManager.get("config/controls"), controlsPanel);
	tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

	/**
	 * ====== SCREENSHOTS TAB =======
	 */

	/** SCREENSHOTS CONFIG **/
	JPanel screenshots = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
	screenshots.setBorder(new TitledBorder(new MatteBorder(new Insets(thick, 0, 0, 0), bcol), txt("gui.screencapture"), just, pos));

	JTextArea screenshotsInfo = new JTextArea(txt("gui.screencapture.info")) {
	    @Override
	    public void setBorder(Border border) {
		// No!
	    }
	};
	screenshotsInfo.setEditable(false);
	screenshotsInfo.setBackground(transparent);
	screenshotsInfo.setForeground(darkgreen);

	// SCREENSHOTS LOCATION
	JLabel screenshotsLocationLabel = new JLabel(txt("gui.screenshots.save") + ":");
	File currentLocation = new File(GlobalConf.screenshot.SCREENSHOT_FOLDER);
	String dirText = txt("gui.screenshots.directory.choose");
	if (currentLocation.exists() && currentLocation.isDirectory()) {
	    dirText = GlobalConf.screenshot.SCREENSHOT_FOLDER;
	}
	final WebButton screenshotsLocation = new WebButton(dirText);
	screenshotsLocation.addActionListener(new ActionListener()
	{
	    private WebDirectoryChooser directoryChooser = null;

	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
		if (directoryChooser == null)
		{
		    directoryChooser = new WebDirectoryChooser(frame, txt("gui.directory.chooseany"));
		    // Increase scrollbar speed
		    JScrollPane wsp = (JScrollPane) ((Container) ((Container) ((Container) ((Container) ((Container) directoryChooser.getComponents()[0]).getComponents()[1]).getComponents()[0]).getComponents()[0]).getComponents()[1]).getComponents()[1];
		    wsp.getVerticalScrollBar().setUnitIncrement(50);
		    File currentLocation = new File(GlobalConf.screenshot.SCREENSHOT_FOLDER);
		    if (currentLocation.exists() && currentLocation.isDirectory()) {
			directoryChooser.setSelectedDirectory(new File(GlobalConf.screenshot.SCREENSHOT_FOLDER));
		    }
		}
		directoryChooser.setVisible(true);

		if (directoryChooser.getResult() == DialogOptions.OK_OPTION)
		{
		    File file = directoryChooser.getSelectedDirectory();
		    screenshotsLocation.setIcon(FileUtils.getFileIcon(file));
		    screenshotsLocation.setText(file.getAbsolutePath());
		}
	    }
	});

	// SCREENSHOT WIDTH AND HEIGHT
	final JSpinner sswidthField = new JSpinner(new SpinnerNumberModel(GlobalConf.screenshot.SCREENSHOT_WIDTH, 100, 5000, 1));
	final JSpinner ssheightField = new JSpinner(new SpinnerNumberModel(GlobalConf.screenshot.SCREENSHOT_HEIGHT, 100, 5000, 1));

	JPanel screenshotSize = new JPanel(new MigLayout("", "[][grow,fill][][grow,fill]", "[][]4[][]"));
	screenshotSize.add(new JLabel(txt("gui.width") + ":"));
	screenshotSize.add(sswidthField);
	screenshotSize.add(new JLabel(txt("gui.height") + ":"));
	screenshotSize.add(ssheightField);

	screenshots.add(screenshotsInfo, "span,wrap");
	screenshots.add(screenshotsLocationLabel);
	screenshots.add(screenshotsLocation, "wrap");
	screenshots.add(screenshotSize, "span");

	JPanel screenshotsPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	screenshotsPanel.add(screenshots, "wrap");

	tabbedPane.addTab(txt("gui.screenshots"), IconManager.get("config/screenshots"), screenshotsPanel);
	tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

	/**
	 * ====== FRAME OUTPUT TAB =======
	 */

	/** IMAGE OUTPUT CONFIG **/
	JPanel imageOutput = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
	imageOutput.setBorder(new TitledBorder(new MatteBorder(new Insets(thick, 0, 0, 0), bcol), txt("gui.frameoutput"), just, pos));

	JTextArea frameInfo = new JTextArea(txt("gui.frameoutput.info")) {
	    @Override
	    public void setBorder(Border border) {
		// No!
	    }
	};
	frameInfo.setEditable(false);
	frameInfo.setBackground(transparent);
	frameInfo.setForeground(darkgreen);

	// SAVE LOCATION
	File currentFrameLocation = new File(GlobalConf.screenshot.SCREENSHOT_FOLDER);
	String dirFrameText = txt("gui.frameoutput.directory.choose");
	if (currentFrameLocation.exists() && currentFrameLocation.isDirectory()) {
	    dirFrameText = GlobalConf.frame.RENDER_FOLDER;
	}
	final WebButton frameLocation = new WebButton(dirFrameText);
	frameLocation.addActionListener(new ActionListener()
	{
	    private WebDirectoryChooser directoryChooser = null;

	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
		if (directoryChooser == null)
		{
		    directoryChooser = new WebDirectoryChooser(frame, txt("gui.directory.chooseany"));
		    // Increase scrollbar speed
		    WebScrollPane wsp = (WebScrollPane) ((Container) ((Container) ((Container) ((Container) ((Container) directoryChooser.getComponents()[0]).getComponents()[1]).getComponents()[0]).getComponents()[0]).getComponents()[1]).getComponents()[1];
		    wsp.getVerticalScrollBar().setUnitIncrement(50);
		    File currentLocation = new File(GlobalConf.frame.RENDER_FOLDER);
		    if (currentLocation.exists() && currentLocation.isDirectory()) {
			directoryChooser.setSelectedDirectory(new File(GlobalConf.frame.RENDER_FOLDER));
		    }
		}
		directoryChooser.setVisible(true);

		if (directoryChooser.getResult() == DialogOptions.OK_OPTION)
		{
		    File file = directoryChooser.getSelectedDirectory();
		    frameLocation.setIcon(FileUtils.getFileIcon(file));
		    frameLocation.setText(file.getAbsolutePath());
		}
	    }
	});

	// NAME
	final JTextField frameFileName = new JTextField();
	frameFileName.getDocument().addDocumentListener(new DocumentListener() {
	    public void changedUpdate(DocumentEvent e) {
		warn();
	    }

	    public void removeUpdate(DocumentEvent e) {
		warn();
	    }

	    public void insertUpdate(DocumentEvent e) {
		warn();
	    }

	    public void warn() {
		String text = frameFileName.getText();
		// Only word characters
		if (!text.matches("^\\w+$")) {
		    frameFileName.setForeground(Color.red);
		} else {
		    frameFileName.setForeground(Color.black);
		}
	    }
	});
	frameFileName.setText(GlobalConf.frame.RENDER_FILE_NAME);

	// FRAME OUTPUT WIDTH AND HEIGHT
	final JSpinner frameWidthField = new JSpinner(new SpinnerNumberModel(GlobalConf.frame.RENDER_WIDTH, 100, 5000, 1));
	final JSpinner frameHeightField = new JSpinner(new SpinnerNumberModel(GlobalConf.frame.RENDER_HEIGHT, 100, 5000, 1));

	JPanel renderSize = new JPanel(new MigLayout("", "[][grow,fill][][grow,fill]", "[][]4[][]"));
	renderSize.add(new JLabel(txt("gui.width") + ":"));
	renderSize.add(frameWidthField);
	renderSize.add(new JLabel(txt("gui.height") + ":"));
	renderSize.add(frameHeightField);

	// TARGET FPS
	final JSpinner targetFPS = new JSpinner(new SpinnerNumberModel(GlobalConf.frame.RENDER_TARGET_FPS, 1, 60, 1));

	// FRAME OUTPUT CHECKBOX
	imageOutput.add(frameInfo, "span");
	imageOutput.add(new JLabel(txt("gui.frameoutput.location") + ":"));
	imageOutput.add(frameLocation, "wrap");
	imageOutput.add(new JLabel(txt("gui.frameoutput.prefix") + ":"));
	imageOutput.add(frameFileName, "wrap");
	imageOutput.add(renderSize, "span");
	imageOutput.add(new JLabel(txt("gui.frameoutput.fps") + ":"));
	imageOutput.add(targetFPS);

	JPanel imageOutputPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	imageOutputPanel.add(imageOutput, "wrap");

	tabbedPane.addTab(txt("gui.frameoutput.title"), IconManager.get("config/frameoutput"), imageOutputPanel);
	tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);

	/**
	 * ====== DATA TAB =======
	 */
	JPanel datasource = new JPanel(new MigLayout("", "[][grow,fill][]", ""));
	datasource.setBorder(new TitledBorder(new MatteBorder(new Insets(thick, 0, 0, 0), bcol), txt("gui.data.source"), just, pos));

	// LOCAL DATA OR OBJECT SERVER RADIO BUTTONS
	final JRadioButton local = new JRadioButton(txt("gui.data.local"));
	local.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		GlobalConf.data.DATA_SOURCE_LOCAL = local.isSelected();
	    }
	});
	local.setSelected(true);

	ButtonGroup dataButtons = new ButtonGroup();
	dataButtons.add(local);

	datasource.add(local, "span,wrap");

	final JPanel dataPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	dataPanel.add(datasource, "wrap");

	tabbedPane.addTab(txt("gui.data"), IconManager.get("config/data"), dataPanel);
	tabbedPane.setMnemonicAt(6, KeyEvent.VK_7);

	// Do not show again
	final JCheckBox showAgain = new JCheckBox(txt("gui.notagain"));
	showAgain.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		GlobalConf.program.SHOW_CONFIG_DIALOG = !showAgain.isSelected();
	    }
	});

	body.add(tabbedPane, "wrap");
	body.add(checkPanel, "wrap");
	if (startup) {
	    body.add(showAgain);
	}

	/** BUTTONS **/
	JPanel buttons = new JPanel(new MigLayout("", "push[][]", ""));

	okButton = new JButton(startup ? txt("gui.launchapp") : txt("gui.saveprefs"));
	okButton.addActionListener(new ActionListener() {
	    boolean goahead;

	    @Override
	    public void actionPerformed(ActionEvent e) {
		goahead = true;

		if (goahead) {
		    // Add all properties to GlobalConf.instance
		    GlobalConf.screen.FULLSCREEN = fullscreen.isSelected();

		    // Fullscreen options
		    GlobalConf.screen.FULLSCREEN_WIDTH = ((DisplayMode) fullScreenResolutions.getSelectedItem()).width;
		    GlobalConf.screen.FULLSCREEN_HEIGHT = ((DisplayMode) fullScreenResolutions.getSelectedItem()).height;

		    // Windowed options
		    GlobalConf.screen.SCREEN_WIDTH = ((Integer) widthField.getValue());
		    GlobalConf.screen.SCREEN_HEIGHT = ((Integer) heightField.getValue());
		    GlobalConf.screen.RESIZABLE = resizable.isSelected();

		    // Graphics
		    ThreadComboBoxBean bean = (ThreadComboBoxBean) msaa.getSelectedItem();
		    GlobalConf.postprocess.POSTPROCESS_ANTIALIAS = bean.value;
		    GlobalConf.screen.VSYNC = vsync.isSelected();

		    bean = (ThreadComboBoxBean) pixelRenderer.getSelectedItem();
		    int oldPx = GlobalConf.scene.PIXEL_RENDERER;
		    GlobalConf.scene.PIXEL_RENDERER = bean.value;
		    if (oldPx != bean.value) {
			// Issue command
			EventManager.instance.post(Events.PIXEL_RENDERER_UPDATE);
		    }

		    // Interface
		    LangComboBoxBean lbean = (LangComboBoxBean) lang.getSelectedItem();
		    GlobalConf.program.LOCALE = lbean.locale.toLanguageTag();
		    if (!I18n.forceinit("./data/i18n/gsbundle"))
			I18n.forceinit("../android/assets/i18n/gsbundle");
		    GlobalConf.program.UI_THEME = (String) theme.getSelectedItem();

		    // Performance
		    bean = (ThreadComboBoxBean) numThreads.getSelectedItem();
		    GlobalConf.performance.NUMBER_THREADS = bean.value;
		    GlobalConf.performance.MULTITHREADING = multithreadCb.isSelected();

		    // Screenshots
		    File ssfile = new File(screenshotsLocation.getText());
		    if (ssfile.exists() && ssfile.isDirectory())
			GlobalConf.screenshot.SCREENSHOT_FOLDER = ssfile.getAbsolutePath();
		    GlobalConf.screenshot.SCREENSHOT_WIDTH = ((Integer) sswidthField.getValue());
		    GlobalConf.screenshot.SCREENSHOT_HEIGHT = ((Integer) ssheightField.getValue());

		    // Frame output
		    File fofile = new File(frameLocation.getText());
		    if (fofile.exists() && fofile.isDirectory())
			GlobalConf.frame.RENDER_FOLDER = fofile.getAbsolutePath();
		    String text = frameFileName.getText();
		    if (text.matches("^\\w+$")) {
			GlobalConf.frame.RENDER_FILE_NAME = text;
		    }
		    GlobalConf.frame.RENDER_WIDTH = ((Integer) frameWidthField.getValue());
		    GlobalConf.frame.RENDER_HEIGHT = ((Integer) frameHeightField.getValue());
		    GlobalConf.frame.RENDER_TARGET_FPS = ((Integer) targetFPS.getValue());

		    // Save configuration
		    try {
			GlobalConf.saveProperties(new File(System.getProperty("properties.file")).toURI().toURL());
		    } catch (MalformedURLException e1) {
			EventManager.instance.post(Events.JAVA_EXCEPTION, e);
		    }

		    EventManager.instance.post(Events.PROPERTIES_WRITTEN);

		    if (startup) {
			gsd.launchMainApp();
		    }
		    frame.setVisible(false);
		}
	    }

	});
	okButton.setMinimumSize(new Dimension(100, 20));

	cancelButton = new JButton(txt("gui.cancel"));
	cancelButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (frame.isDisplayable()) {
		    frame.dispose();
		}
	    }
	});
	cancelButton.setMinimumSize(new Dimension(100, 20));

	buttons.add(okButton);
	buttons.add(cancelButton);

	frame.add(body, BorderLayout.CENTER);
	frame.add(buttons, BorderLayout.SOUTH);

    }

    private void enableComponents(boolean enabled, JComponent... components) {
	for (JComponent c : components) {
	    if (c != null)
		c.setEnabled(enabled);
	}
    }

    private void selectFullscreen(boolean fullscreen, JSpinner widthField, JSpinner heightField, JComboBox<DisplayMode> fullScreenResolutions, JCheckBox resizable) {
	if (fullscreen) {
	    GlobalConf.screen.SCREEN_WIDTH = ((DisplayMode) fullScreenResolutions.getSelectedItem()).width;
	    GlobalConf.screen.SCREEN_HEIGHT = ((DisplayMode) fullScreenResolutions.getSelectedItem()).height;
	} else {
	    GlobalConf.screen.SCREEN_WIDTH = (Integer) widthField.getValue();
	    GlobalConf.screen.SCREEN_HEIGHT = (Integer) heightField.getValue();
	}

	enableComponents(!fullscreen, widthField, heightField, resizable);
	enableComponents(fullscreen, fullScreenResolutions);
    }

    private int idxAa(int base, int x) {
	if (x == -1)
	    return 1;
	if (x == -2)
	    return 2;
	if (x == 0)
	    return 0;
	return (int) (Math.log(x) / Math.log(2) + 1e-10) + 2;
    }

    private int idxLang(String code, LangComboBoxBean[] langs) {
	if (code.isEmpty()) {
	    code = I18n.bundle.getLocale().toLanguageTag();
	}
	for (int i = 0; i < langs.length; i++) {
	    if (langs[i].locale.toLanguageTag().equals(code)) {
		return i;
	    }
	}
	return -1;
    }

    private class ThreadComboBoxBean {
	public String name;
	public int value;

	public ThreadComboBoxBean(String name, int samples) {
	    super();
	    this.name = name;
	    this.value = samples;
	}

	@Override
	public String toString() {
	    return name;
	}

    }

    private class LangComboBoxBean implements Comparable<LangComboBoxBean> {
	public Locale locale;
	public String name;

	public LangComboBoxBean(Locale locale) {
	    super();
	    this.locale = locale;
	    this.name = GlobalResources.capitalise(locale.getDisplayName(locale));
	}

	@Override
	public String toString() {
	    return name;
	}

	@Override
	public int compareTo(LangComboBoxBean o) {
	    return this.name.compareTo(o.name);
	}

    }

    private Thread getCheckVersionThread() {
	return new Thread(new CallbackTask(new VersionChecker(GlobalConf.program.VERSION_CHECK_URL), new Callback() {
	    @Override
	    public void complete(Object result) {
		checkPanel.removeAll();
		checkPanel.add(checkLabel);
		if (result instanceof String) {
		    // Error
		    checkLabel.setText("Error checking version: " + (String) result);
		    checkLabel.setForeground(Color.RED);
		} else if (result instanceof JsonValue) {
		    // Ok!
		    JsonValue json = (JsonValue) result;

		    JsonValue last = json.get(json.size - 1);
		    String version = last.getString("name");
		    if (version.matches("^(\\D{1})?\\d+.\\d+(\\D{1})?$")) {
			GlobalConf.program.LAST_VERSION = new String(version);
			GlobalConf.program.LAST_CHECKED = new Date();
			newVersionCheck(version);
		    }
		    checkPanel.validate();
		}

	    }
	}));
    }

    /**
     * Checks the given version against the current version and:
     * <ul><li>
     * Displays a "new version available" message if the given version is newer than the current.
     * </li><li>
     * Display a "you have the latest version" message and a "check now" button if the given version is older.
     * </li></ul>
     * @param version The version to check.
     */
    private void newVersionCheck(String version) {
	int[] majmin = GlobalConf.VersionConf.getMajorMinorFromString(version);

	if (majmin[0] > GlobalConf.version.major || (majmin[0] == GlobalConf.version.major && majmin[1] > GlobalConf.version.minor)) {
	    // There's a new version!
	    checkLabel.setText(txt("gui.newversion.available", GlobalConf.version, version));
	    try {
		final URI uri = new URI(GlobalConf.WEBPAGE);

		JButton button = new JButton();
		button.setText(txt("gui.newversion.getit"));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setBorderPainted(false);
		button.setOpaque(false);
		button.setBackground(Color.WHITE);
		button.setToolTipText(uri.toString());
		button.addActionListener(new ActionListener() {

		    @Override
		    public void actionPerformed(ActionEvent e) {
			if (Desktop.isDesktopSupported()) {
			    try {
				Desktop.getDesktop().browse(uri);
			    } catch (IOException ex) {
			    }
			} else {
			}
		    }

		});
		checkPanel.add(button);
	    } catch (URISyntaxException e1) {
	    }
	} else {
	    checkLabel.setText(txt("gui.newversion.nonew", GlobalConf.program.getLastCheckedString()));
	    // Add check now button
	    JButton button = new JButton();
	    button.setText(txt("gui.newversion.checknow"));
	    button.setHorizontalAlignment(SwingConstants.LEFT);
	    button.setBorderPainted(false);
	    button.setOpaque(false);
	    button.setBackground(Color.WHITE);
	    button.setToolTipText(txt("gui.newversion.checknow.tooltip"));
	    button.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    getCheckVersionThread().start();
		}

	    });
	    checkPanel.add(button);
	}
	checkLabel.setForeground(darkgreen);
    }

    private String keysToString(TreeSet<Integer> keys) {
	String s = "";

	int i = 0;
	int n = keys.size();
	for (Integer key : keys) {
	    s += Keys.toString(key).toUpperCase();
	    if (i < n - 1) {
		s += " + ";
	    }

	    i++;
	}

	return s;
    }

    class JXTabbedPane extends JTabbedPane {

	private ITabRenderer tabRenderer = new DefaultTabRenderer();

	public JXTabbedPane() {
	    super();
	}

	public JXTabbedPane(int tabPlacement) {
	    super(tabPlacement);
	}

	public JXTabbedPane(int tabPlacement, int tabLayoutPolicy) {
	    super(tabPlacement, tabLayoutPolicy);
	}

	public ITabRenderer getTabRenderer() {
	    return tabRenderer;
	}

	public void setTabRenderer(ITabRenderer tabRenderer) {
	    this.tabRenderer = tabRenderer;
	}

	@Override
	public void addTab(String title, Component component) {
	    this.addTab(title, null, component, null);
	}

	@Override
	public void addTab(String title, Icon icon, Component component) {
	    this.addTab(title, icon, component, null);
	}

	@Override
	public void addTab(String title, Icon icon, Component component, String tip) {
	    super.addTab(title, icon, component, tip);
	    int tabIndex = getTabCount() - 1;
	    Component tab = tabRenderer.getTabRendererComponent(this, title, icon, tabIndex);
	    super.setTabComponentAt(tabIndex, tab);
	}
    }

    interface ITabRenderer {

	public Component getTabRendererComponent(JTabbedPane tabbedPane, String text, Icon icon, int tabIndex);

    }

    abstract class AbstractTabRenderer implements ITabRenderer {

	private String prototypeText = "";
	private Icon prototypeIcon = UIManager.getIcon("OptionPane.informationIcon");
	private int horizontalTextAlignment = SwingConstants.CENTER;
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public AbstractTabRenderer() {
	    super();
	}

	public void setPrototypeText(String text) {
	    String oldText = this.prototypeText;
	    this.prototypeText = text;
	    firePropertyChange("prototypeText", oldText, text);
	}

	public String getPrototypeText() {
	    return prototypeText;
	}

	public Icon getPrototypeIcon() {
	    return prototypeIcon;
	}

	public void setPrototypeIcon(Icon icon) {
	    Icon oldIcon = this.prototypeIcon;
	    this.prototypeIcon = icon;
	    firePropertyChange("prototypeIcon", oldIcon, icon);
	}

	public int getHorizontalTextAlignment() {
	    return horizontalTextAlignment;
	}

	public void setHorizontalTextAlignment(int horizontalTextAlignment) {
	    this.horizontalTextAlignment = horizontalTextAlignment;
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return propertyChangeSupport.getPropertyChangeListeners();
	}

	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
	    return propertyChangeSupport.getPropertyChangeListeners(propertyName);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	    propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
	    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	    PropertyChangeListener[] listeners = getPropertyChangeListeners();
	    for (int i = listeners.length - 1; i >= 0; i--) {
		listeners[i].propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
	    }
	}
    }

    class DefaultTabRenderer extends AbstractTabRenderer implements PropertyChangeListener {

	private Component prototypeComponent;

	public DefaultTabRenderer() {
	    super();
	    prototypeComponent = generateRendererComponent(getPrototypeText(), getPrototypeIcon(), getHorizontalTextAlignment());
	    addPropertyChangeListener(this);
	}

	private Component generateRendererComponent(String text, Icon icon, int horizontalTabTextAlignmen) {
	    JPanel rendererComponent = new JPanel(new GridBagLayout());
	    rendererComponent.setOpaque(false);

	    GridBagConstraints c = new GridBagConstraints();
	    c.insets = new Insets(2, 4, 2, 4);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    rendererComponent.add(new JLabel(icon), c);

	    c.gridx = 1;
	    c.weightx = 1;
	    rendererComponent.add(new JLabel(text, horizontalTabTextAlignmen), c);

	    return rendererComponent;
	}

	@Override
	public Component getTabRendererComponent(JTabbedPane tabbedPane, String text, Icon icon, int tabIndex) {
	    Component rendererComponent = generateRendererComponent(text, icon, getHorizontalTextAlignment());
	    int prototypeWidth = prototypeComponent.getPreferredSize().width;
	    int prototypeHeight = prototypeComponent.getPreferredSize().height;
	    rendererComponent.setPreferredSize(new Dimension(prototypeWidth, prototypeHeight));
	    return rendererComponent;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    String propertyName = evt.getPropertyName();
	    if ("prototypeText".equals(propertyName) || "prototypeIcon".equals(propertyName)) {
		this.prototypeComponent = generateRendererComponent(getPrototypeText(), getPrototypeIcon(), getHorizontalTextAlignment());
	    }
	}
    }

}
