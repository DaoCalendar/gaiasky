/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.util.scene2d;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import gaiasky.GaiaSky;
import gaiasky.desktop.util.SysUtils;
import gaiasky.interafce.GenericDialog;
import gaiasky.util.GlobalConf;
import gaiasky.util.I18n;
import gaiasky.util.Logger;
import gaiasky.util.TextUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

/**
 * A simple file chooser for scene2d.ui
 */
public class FileChooser extends GenericDialog {
    private static final Logger.Log logger = Logger.getLogger(FileChooser.class);

    public interface ResultListener {
        boolean result(boolean success, Path result);
    }

    /**
     * The type of files that can be chosen with this file chooser
     */
    public enum FileChooserTarget {
        FILES,
        DIRECTORIES,
        ALL
    }

    /** Target of this file chooser **/
    final private FileChooserTarget target;
    private boolean fileNameEnabled;
    private TextField fileNameInput;
    private Label fileNameLabel, acceptedFiles;
    private Path baseDir;
    private OwnTextField location;
    private List<FileListItem> fileList;
    private Table controlsTable;
    private HorizontalGroup driveButtonsList;
    private Array<TextButton> driveButtons;
    private float scrollPaneWidth, scrollPanelHeight, maxPathLength;
    private ScrollPane scrollPane;
    private CheckBox hidden;

    private Path currentDir, previousDir, nextDir;
    protected String result;

    private boolean showHidden = false;

    protected ResultListener resultListener;
    protected EventListener selectionListener;

    private static final Comparator<FileListItem> dirListComparator = (file1, file2) -> {
        if (Files.isDirectory(file1.file) && !Files.isDirectory(file2.file)) {
            return -1;
        }
        if (Files.isDirectory(file1.file) && Files.isDirectory(file2.file)) {
            return file1.name.compareTo(file2.name);
        }
        if (!Files.isDirectory(file1.file) && !Files.isDirectory(file2.file)) {
            return file1.name.compareTo(file2.name);
        }
        return 1;
    };
    /** Filters files that appear in the file chooser. For internal use only! **/
    private DirectoryStream.Filter<Path> filter;
    /** Enables directories in the file chooser **/
    private final boolean directoryBrowsingEnabled;
    /** Enables files in the file chooser **/
    private final boolean fileBrowsingEnabled;
    /** Allows setting filters on the files which are to be selected **/
    private PathnameFilter pathnameFilter;

    public FileChooser(String title, final Skin skin, Stage stage, Path baseDir, FileChooserTarget target) {
        this(title, skin, stage, baseDir, target, null);
    }

    public FileChooser(String title, final Skin skin, Stage stage, Path baseDir, FileChooserTarget target, EventListener selectionListener) {
        this(title, skin, stage, baseDir, target, selectionListener, true);
    }

    public FileChooser(String title, final Skin skin, Stage stage, Path baseDir, FileChooserTarget target, EventListener selectionListener, boolean directoryBrowsingEnabled) {
        super(title, skin, stage);
        this.baseDir = baseDir;
        this.selectionListener = selectionListener;
        this.target = target;
        // Browse files if we are picking files
        this.fileBrowsingEnabled = target == FileChooserTarget.FILES;
        // Browse directories
        this.directoryBrowsingEnabled = directoryBrowsingEnabled;

        setCancelText(I18n.txt("gui.close"));
        setAcceptText(I18n.txt("gui.select"));

        buildSuper();
    }

    @Override
    public void build() {
        scrollPaneWidth = 600 * GlobalConf.UI_SCALE_FACTOR;
        scrollPanelHeight = 450 * GlobalConf.UI_SCALE_FACTOR;
        maxPathLength = GlobalConf.isHiDPI() ? 9.5f : 5.5f;

        content.top().left();
        content.defaults().space(5 * GlobalConf.UI_SCALE_FACTOR);
        this.padLeft(10 * GlobalConf.UI_SCALE_FACTOR);
        this.padRight(10 * GlobalConf.UI_SCALE_FACTOR);

        // Controls
        controlsTable = new Table(skin);
        OwnTextIconButton home = new OwnTextIconButton("", skin, "home");
        home.addListener(new OwnTextTooltip(I18n.txt("gui.fc.home"), skin));
        home.addListener(event -> {
            if (event instanceof ChangeEvent) {
                try {
                    previousDir = currentDir;
                    changeDirectory(SysUtils.getHomeDir());
                } catch (IOException e) {
                    logger.error(e);
                }
                lastClick = 0;
                return true;
            }
            return false;
        });
        OwnTextIconButton back = new OwnTextIconButton("", skin, "back");
        back.addListener(new OwnTextTooltip(I18n.txt("gui.fc.back"), skin));
        back.addListener(event -> {
            if (event instanceof ChangeEvent) {
                try {
                    if (previousDir != null) {
                        nextDir = currentDir;
                        changeDirectory(previousDir);
                    }
                } catch (IOException e) {
                    logger.error(e);
                }
                lastClick = 0;
                return true;
            }
            return false;
        });
        OwnTextIconButton fwd = new OwnTextIconButton("", skin, "forward");
        fwd.addListener(new OwnTextTooltip(I18n.txt("gui.fc.forward"), skin));
        fwd.addListener(event -> {
            if (event instanceof ChangeEvent) {
                try {
                    if (nextDir != null) {
                        previousDir = currentDir;
                        changeDirectory(nextDir);
                    }
                } catch (IOException e) {
                    logger.error(e);
                }
                lastClick = 0;
                return true;
            }
            return false;
        });
        OwnTextIconButton parent = new OwnTextIconButton("", skin, "up");
        parent.addListener(new OwnTextTooltip(I18n.txt("gui.fc.parent"), skin));
        parent.addListener(event -> {
            if (event instanceof ChangeEvent) {
                try {
                    if (currentDir.getParent() != null) {
                        previousDir = currentDir;
                        changeDirectory(currentDir.getParent());
                    }
                } catch (IOException e) {
                    logger.error(e);
                }
                lastClick = 0;
                return true;
            }
            return false;
        });

        controlsTable.add(home).left().padRight(pad5);
        controlsTable.add(back).left().padRight(pad5);
        controlsTable.add(parent).left().padRight(pad5);
        controlsTable.add(fwd).left().padRight(pad5).padRight(pad10);

        // Text input with current location
        location = new OwnTextField("", skin);
        location.setWidth(465f * GlobalConf.UI_SCALE_FACTOR);
        location.setAlignment(Align.left);
        location.addListener(event -> {
            if (event instanceof ChangeEvent) {
                try {
                    Path locPath = Path.of(location.getText());
                    if (Files.exists(locPath) && Files.isDirectory(locPath)) {
                        previousDir = currentDir;
                        changeDirectory(locPath);
                        GaiaSky.postRunnable(()-> {
                            stage.setKeyboardFocus(location);
                            location.setCursorPosition(location.getText().length());
                        });
                    }
                } catch (IOException e) {
                    logger.error(e);
                }
                return true;
            }
            return false;
        });
        controlsTable.add(location).left().pad(pad10);

        // In windows, we need to be able to change drives
        driveButtonsList = new HorizontalGroup();
        driveButtonsList.left().space(10 * GlobalConf.UI_SCALE_FACTOR);
        driveButtonsList.addActor(new OwnLabel(I18n.txt("gui.fc.drives") + ":", skin));
        Iterable<Path> drives = FileSystems.getDefault().getRootDirectories();
        driveButtons = new Array<>();
        for (Path drive : drives) {
            TextButton driveButton = new OwnTextIconButton(drive.toString(), skin, "drive");
            driveButton.addListener(new OwnTextTooltip(I18n.txt("gui.fc.drive", drive.toString()), skin));
            driveButton.addListener(event -> {
                if (event instanceof ChangeEvent) {
                    try {
                        changeDirectory(drive);
                    } catch (IOException e) {
                        logger.error(e);
                    }
                    lastClick = 0;
                    return true;
                }
                return false;
            });
            driveButtons.add(driveButton);
            driveButtonsList.addActor(driveButton);
        }

        acceptedFiles = new Label("", skin, "sc-header");
        acceptedFiles.setAlignment(Align.right);

        fileList = new List<>(skin, "light");
        scrollPane = new OwnScrollPane(fileList, skin, "minimalist");
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setSmoothScrolling(true);
        scrollPane.setFadeScrollBars(false);
        fileList.getSelection().setProgrammaticChangeEvents(false);
        if (selectionListener != null)
            fileList.addListener(selectionListener);
        // Lookup with keyboard
        fileList.addListener(event -> {
            if (event instanceof InputEvent) {
                InputEvent ie = (InputEvent) event;
                if (ie.getType() == Type.keyTyped) {
                    char ch = ie.getCharacter();
                    Array<FileListItem> l = fileList.getItems();
                    FileListItem toSelect = null;
                    for (FileListItem fli : l) {
                        if (Character.toUpperCase(fli.name.charAt(0)) == Character.toUpperCase(ch)) {
                            toSelect = fli;
                            break;
                        }
                    }
                    if (toSelect != null) {
                        fileList.setSelected(toSelect);
                        int si = fileList.getSelectedIndex();
                        float px = si * fileList.getItemHeight();
                        scrollPane.setScrollY(px);
                    }
                }
            }
            return false;
        });
        // Select items
        fileList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final FileListItem selected = fileList.getSelected();
                result = selected.name;
                fileNameInput.setText(result);
                lastClick = 0;
            }
        });
        // Double-click behaviour
        fileList.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final FileListItem selected = fileList.getSelected();
                if (TimeUtils.millis() - lastClick < 250) {
                    Path sel = selected.file;
                    // Double click
                    try {
                        if (directoryBrowsingEnabled && Files.isDirectory(sel)) {
                            // Change directory
                            if (selected.name.trim().equals("..")) {
                                // Going back, set next
                                nextDir = currentDir;
                            } else {
                                // Set last
                                previousDir = currentDir;
                            }
                            changeDirectory(sel);
                            lastClick = 0;
                        } else if (target == FileChooserTarget.FILES && (filter == null || filter.accept(sel))) {
                            // Accept
                            acceptButton.fire(new ChangeEvent());

                            lastClick = 0;
                        }
                    } catch (IOException e) {
                        logger.error(e);
                        lastClick = 0;
                    }
                } else if (event.getType() == Type.touchUp) {
                    lastClick = TimeUtils.millis();
                }
            }
        });

        fileNameInput = new TextField("", skin);
        fileNameLabel = new Label(I18n.txt("gui.fc.filename") + ":", skin);
        fileNameInput.setTextFieldListener((textField, c) -> result = textField.getText());

        hidden = new OwnCheckBox("Show hidden", skin, 5 * GlobalConf.UI_SCALE_FACTOR);
        hidden.setChecked(false);
        hidden.addListener(event -> {
            if (event instanceof ChangeEvent) {
                this.showHidden = hidden.isChecked();
                try {
                    changeDirectory(currentDir);
                } catch (IOException e) {
                    logger.error(e);
                }
                return true;
            }
            return false;
        });

        filter = pathname -> {
            boolean root = (Files.isDirectory(pathname) && directoryBrowsingEnabled) || (Files.isRegularFile(pathname) && fileBrowsingEnabled);
            if (root && pathnameFilter != null && Files.isRegularFile(pathname)) {
                root = pathnameFilter.accept(pathname);
            }
            return root;
        };
        setTargetListener();

        content.add(acceptedFiles).top().left().row();
        content.add(driveButtonsList).top().left().row();
        content.add(controlsTable).top().left().row();
        content.add(scrollPane).size(scrollPaneWidth, scrollPanelHeight).left().fill().expand().row();
        content.add(hidden).top().left().row();
        if (fileNameEnabled) {
            content.add(fileNameLabel).fillX().expandX().row();
            content.add(fileNameInput).fillX().expandX().row();
            stage.setKeyboardFocus(fileNameInput);
        }

        try {
            changeDirectory(baseDir);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private void changeDirectory(Path directory) throws IOException {
        Path lastDir = directory != currentDir ? currentDir : null;
        currentDir = directory;
        String path = currentDir.toAbsolutePath().toString();

        maxPathLength = 6.5f;
        while (path.length() * maxPathLength > scrollPaneWidth * 0.9f) {
            path = TextUtils.capString(path, path.length() - 4, true);
        }
        location.setText(path);

        final Array<FileListItem> items = new Array<>();

        DirectoryStream<Path> list = Files.newDirectoryStream(directory, filter);
        for (final Path p : list) {
            // Only list hidden if user chose it
            if (showHidden || !p.getFileName().toString().startsWith(".")) {
                if (pathnameFilter != null && pathnameFilter.accept(p) || Files.isDirectory(p) && directoryBrowsingEnabled) {
                    FileListItem fli = new FileListItem(p);
                    items.add(fli);
                }
            }
        }

        items.sort(dirListComparator);

        if (directory.getParent() != null) {
            items.insert(0, new FileListItem("..", directory.getParent()));
        }

        acceptButton.setDisabled(true);
        fileList.setItems(items);
        scrollPane.layout();

        if (lastDir != null && lastDir.getParent() != null && lastDir.getParent().equals(currentDir)) {
            // select last if we're going back
            Array<FileListItem> l = fileList.getItems();
            for (FileListItem fli : l) {
                if (fli.file.equals(lastDir)) {
                    fileList.setSelected(fli);
                    acceptButton.setDisabled(!isTargetOk(fli.file));
                    break;
                }
            }
            float px = fileList.getItemHeight() * fileList.getSelectedIndex();
            scrollPane.setScrollY(px);
        } else {
            fileList.setSelected(null);
            scrollPane.setScrollY(0);
        }
    }

    private boolean isTargetOk(Path file) {
        switch (target) {
        case FILES:
            return Files.isRegularFile(file);
        case DIRECTORIES:
            return Files.isDirectory(file);
        default:
            return true;
        }
    }

    private void setTargetListener() {
        setSelectionListener(event1 -> {
            if (event1 instanceof ChangeEvent) {
                List<FileChooser.FileListItem> list = (List<FileChooser.FileListItem>) event1.getListenerActor();
                if (list != null) {
                    ArraySelection<FileListItem> as = list.getSelection();
                    if (as != null && as.hasItems()) {
                        FileChooser.FileListItem selected = as.getLastSelected();
                        acceptButton.setDisabled(!isTargetOk(selected.file));
                    }
                }
                return true;
            }
            return false;
        });
    }

    public void setAcceptedFiles(String accepted) {
        acceptedFiles.setText("Accepted: " + accepted);
    }

    public Path getResult() {
        String path = currentDir.toAbsolutePath().toString() + "/";
        if (result != null && result.length() > 0) {
            String folder = currentDir.getFileName().toString();
            if (folder.equals(result)) {
                if (Files.exists(Paths.get(path, result))) {
                    path += result;
                } else {
                    // Nothing
                }
            } else {
                path += result;
            }
        }
        return Paths.get(path);
    }

    /**
     * Overrides the default filter. If you use this, the attributes {@link FileChooser#directoryBrowsingEnabled} and
     * {@file FileChooser#fileBrowsingEnabled} won't have effect anymore. To set additional filters on the
     * path names, use {@link FileChooser#setFileFilter(PathnameFilter)} instead.
     *
     * @param filter The new file filter
     * @return This file chooser
     */
    public FileChooser setFilter(DirectoryStream.Filter<Path> filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Sets the file filter. This filter will be used to check whether file pathnames are accepted or not. It works
     * in conjunction with {@file FileChooser#fileBrowsingEnabled},
     * so you do not need to check whether the pathname is a file.
     *
     * @param f The file filter
     * @return This file chooser
     */
    public FileChooser setFileFilter(PathnameFilter f) {
        this.pathnameFilter = f;
        try {
            changeDirectory(currentDir);
        } catch (IOException e) {
            logger.error(e);
        }

        return this;
    }

    /**
     * Sets a listener which runs when an entry is selected. Useful to show
     * some text, disable items, etc.
     *
     * @param listener The listener
     * @return This file chooser
     */
    private FileChooser setSelectionListener(EventListener listener) {
        if (listener != null) {
            if (this.selectionListener != null)
                fileList.removeListener(this.selectionListener);

            this.selectionListener = listener;
            if (!fileList.getListeners().contains(selectionListener, true)) {
                fileList.addListener(selectionListener);
            }
        }
        return this;
    }

    public FileChooser setFileNameEnabled(boolean fileNameEnabled) {
        this.fileNameEnabled = fileNameEnabled;
        return this;
    }

    public FileChooser setResultListener(ResultListener result) {
        this.resultListener = result;
        return this;
    }

    long lastClick = 0l;

    @Override
    public void accept() {
        if (resultListener != null) {
            resultListener.result(true, getResult());
        }
    }

    @Override
    public void cancel() {
        if (resultListener != null) {
            resultListener.result(false, getResult());
        }
    }

    public class FileListItem {

        public Path file;
        public String name;

        public FileListItem(Path file) {
            this.file = file;
            this.name = file.getFileName().toString();
        }

        public FileListItem(String name, Path file) {
            this.file = file;
            this.name = name;
        }

        public String toString() {
            return " " + name;
        }

    }

    public interface PathnameFilter {
        boolean accept(Path pathname);
    }

}