/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.interafce.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import gaiasky.GaiaSky;
import gaiasky.event.EventManager;
import gaiasky.event.Events;
import gaiasky.event.IObserver;
import gaiasky.interafce.BookmarksManager;
import gaiasky.interafce.BookmarksManager.BNode;
import gaiasky.interafce.ControlsWindow;
import gaiasky.interafce.NewBookmarkFolderDialog;
import gaiasky.scenegraph.IFocus;
import gaiasky.scenegraph.ISceneGraph;
import gaiasky.scenegraph.SceneGraphNode;
import gaiasky.scenegraph.camera.CameraManager.CameraMode;
import gaiasky.scenegraph.camera.NaturalCamera;
import gaiasky.util.GlobalConf;
import gaiasky.util.I18n;
import gaiasky.util.scene2d.*;

import java.util.List;

public class BookmarksComponent extends GuiComponent implements IObserver {
    static private final Vector2 tmpCoords = new Vector2();

    protected ISceneGraph sg;

    protected Tree<TreeNode, String> bookmarksTree;
    protected TextField searchBox;
    protected OwnScrollPane bookmarksScrollPane;

    protected Table infoTable;
    protected Cell infoCell1, infoCell2;
    protected OwnLabel infoMessage1, infoMessage2;

    private boolean events = true;

    private Drawable folderIcon, bookmarkIcon;

    public BookmarksComponent(Skin skin, Stage stage) {
        super(skin, stage);
        folderIcon = skin.getDrawable("iconic-folder-small");
        bookmarkIcon = skin.getDrawable("iconic-bookmark-small");
        EventManager.instance.subscribe(this, Events.FOCUS_CHANGED, Events.BOOKMARKS_ADD, Events.BOOKMARKS_REMOVE, Events.BOOKMARKS_REMOVE_ALL);
    }

    @Override
    public void initialize() {
        float contentWidth = ControlsWindow.getContentWidth();
        searchBox = new OwnTextField("", skin);
        searchBox.setName("search box");
        searchBox.setWidth(contentWidth);
        searchBox.setMessageText(I18n.txt("gui.objects.search"));
        searchBox.addListener(event -> {
            if (event instanceof InputEvent) {
                InputEvent ie = (InputEvent) event;
                if (ie.getType() == Type.keyUp && !searchBox.getText().isEmpty()) {
                    String text = searchBox.getText().toLowerCase().trim();
                    if (sg.containsNode(text)) {
                        SceneGraphNode node = sg.getNode(text);
                        if (node instanceof IFocus) {
                            IFocus focus = (IFocus) node;
                            boolean timeOverflow = focus.isCoordinatesTimeOverflow();
                            boolean ctOn = GaiaSky.instance.isOn(focus.getCt());
                            if (!timeOverflow && ctOn) {
                                GaiaSky.postRunnable(() -> {
                                    EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.FOCUS_MODE, true);
                                    EventManager.instance.post(Events.FOCUS_CHANGE_CMD, focus, true);
                                });
                            } else if (timeOverflow) {
                                info(I18n.txt("gui.objects.search.timerange.1", text), I18n.txt("gui.objects.search.timerange.2"));
                            } else {
                                info(I18n.txt("gui.objects.search.invisible.1", text), I18n.txt("gui.objects.search.invisible.2", focus.getCt().toString()));
                            }
                        }
                    } else {
                        info(null, null);
                    }
                    if (GaiaSky.instance.getICamera() instanceof NaturalCamera)
                        ((NaturalCamera) GaiaSky.instance.getICamera()).getCurrentMouseKbdListener().removePressedKey(ie.getKeyCode());

                    if (ie.getKeyCode() == Keys.ESCAPE) {
                        // Lose focus
                        stage.setKeyboardFocus(null);
                    }
                } else if (ie.getType() == Type.keyDown) {
                    if (ie.getKeyCode() == Keys.CONTROL_LEFT || ie.getKeyCode() == Keys.CONTROL_RIGHT) {
                        // Lose focus
                        stage.setKeyboardFocus(null);
                    }
                }
                return true;
            }
            return false;
        });

        // Info message
        infoTable = new Table(skin);
        infoCell1 = infoTable.add();
        infoTable.row();
        infoCell2 = infoTable.add();

        infoMessage1 = new OwnLabel("", skin, "default-blue");
        infoMessage2 = new OwnLabel("", skin, "default-blue");

        /*
         * OBJECTS
         */
        bookmarksTree = new Tree(skin);
        bookmarksTree.setName("bookmarks tree");
        reloadBookmarksTree();
        bookmarksTree.addListener(event -> {
            if (events)
                if (event instanceof ChangeEvent) {
                    ChangeEvent ce = (ChangeEvent) event;
                    Actor actor = ce.getTarget();
                    TreeNode selected = (TreeNode) ((Tree) actor).getSelectedNode();
                    if (selected != null && !selected.hasChildren()) {
                        String name = selected.getValue();
                        if (sg.containsNode(name)) {
                            SceneGraphNode node = sg.getNode(name);
                            if (node instanceof IFocus) {
                                IFocus focus = (IFocus) node;
                                boolean timeOverflow = focus.isCoordinatesTimeOverflow();
                                boolean ctOn = GaiaSky.instance.isOn(focus.getCt());
                                if (!timeOverflow && ctOn) {
                                    GaiaSky.postRunnable(() -> {
                                        EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.FOCUS_MODE, true);
                                        EventManager.instance.post(Events.FOCUS_CHANGE_CMD, focus, true);
                                    });
                                    info(null, null);
                                } else if (timeOverflow) {
                                    info(I18n.txt("gui.objects.search.timerange.1", name), I18n.txt("gui.objects.search.timerange.2"));
                                } else {
                                    info(I18n.txt("gui.objects.search.invisible.1", name), I18n.txt("gui.objects.search.invisible.2", focus.getCt().toString()));
                                }
                            }
                        } else {
                            info(null, null);
                        }
                    }
                    return true;
                } else if (event instanceof InputEvent) {
                    InputEvent ie = (InputEvent) event;
                    ie.toCoordinates(event.getListenerActor(), tmpCoords);
                    if (ie.getType() == Type.touchDown && ie.getButton() == Input.Buttons.RIGHT) {
                        TreeNode target = bookmarksTree.getNodeAt(tmpCoords.y);
                        // Context menu!
                        if (target != null) {
                            //selectBookmark(target.getValue(), true);
                            GaiaSky.postRunnable(() -> {
                                ContextMenu cm = new ContextMenu(skin, "default");
                                // New folder...
                                BNode parent = target.node.getFirstFolderAncestor();
                                String parentName = "/" + (parent == null ? "" : parent.path.toString());
                                MenuItem newFolder = new MenuItem(I18n.txt("gui.bookmark.context.newfolder", parentName), skin);
                                newFolder.addListener(evt -> {
                                    if (evt instanceof ChangeEvent) {
                                        NewBookmarkFolderDialog nbfd = new NewBookmarkFolderDialog(parent.path.toString(), skin, stage);
                                        nbfd.setAcceptRunnable(() -> {
                                            String folderName = nbfd.input.getText();
                                            EventManager.instance.post(Events.BOOKMARKS_ADD, parent.path.resolve(folderName).toString(), true);
                                            reloadBookmarksTree();
                                        });
                                        nbfd.show(stage);
                                        return true;
                                    }
                                    return false;
                                });
                                cm.addItem(newFolder);
                                // Delete
                                MenuItem delete = new MenuItem(I18n.txt("gui.bookmark.context.delete", target.getValue()), skin);
                                delete.addListener(evt -> {
                                    if (evt instanceof ChangeEvent) {
                                        EventManager.instance.post(Events.BOOKMARKS_REMOVE, target.node.path.toString());
                                        reloadBookmarksTree();
                                        return true;
                                    }
                                    return false;
                                });
                                cm.addItem(delete);

                                cm.add(new Separator(skin, "menu")).padTop(2).padBottom(2).fill().expand().row();
                                // Move to...
                                MenuItem move = new MenuItem(I18n.txt("gui.bookmark.context.move", target.getValue(), "/"), skin);
                                move.addListener(evt -> {
                                    if (evt instanceof ChangeEvent) {
                                        EventManager.instance.post(Events.BOOKMARKS_MOVE, target.node, null);
                                        reloadBookmarksTree();
                                        return true;
                                    }
                                    return false;
                                });
                                cm.addItem(move);
                                List<BNode> folders = BookmarksManager.instance.getFolders();
                                for (BNode folder : folders) {
                                    if (!target.node.isDescendantOf(folder)) {
                                        MenuItem mv = new MenuItem(I18n.txt("gui.bookmark.context.move", target.getValue(), "/" + folder.path.toString()), skin);
                                        mv.addListener(evt -> {
                                            if (evt instanceof ChangeEvent) {
                                                EventManager.instance.post(Events.BOOKMARKS_MOVE, target.node, folder);
                                                reloadBookmarksTree();
                                                return true;
                                            }
                                            return false;
                                        });
                                        cm.addItem(mv);
                                    }
                                }


                                cm.showMenu(stage, Gdx.input.getX(ie.getPointer()), Gdx.graphics.getHeight() - Gdx.input.getY(ie.getPointer()));
                            });
                        } else {
                            // New folder
                            GaiaSky.postRunnable(() -> {
                                ContextMenu cm = new ContextMenu(skin, "default");
                                // New folder...
                                String parentName = "/";
                                MenuItem newFolder = new MenuItem(I18n.txt("gui.bookmark.context.newfolder", parentName), skin);
                                newFolder.addListener(evt -> {
                                    if (evt instanceof ChangeEvent) {
                                        NewBookmarkFolderDialog nbfd = new NewBookmarkFolderDialog("/", skin, stage);
                                        nbfd.setAcceptRunnable(() -> {
                                            String folderName = nbfd.input.getText();
                                            EventManager.instance.post(Events.BOOKMARKS_ADD, folderName, true);
                                            reloadBookmarksTree();
                                        });
                                        nbfd.show(stage);
                                        return true;
                                    }
                                    return false;
                                });
                                cm.addItem(newFolder);
                                cm.showMenu(stage, Gdx.input.getX(ie.getPointer()), Gdx.graphics.getHeight() - Gdx.input.getY(ie.getPointer()));
                            });
                        }
                    }
                }
            return false;
        });

        bookmarksScrollPane = new OwnScrollPane(bookmarksTree, skin, "minimalist-nobg");
        bookmarksScrollPane.setName("bookmarks scroll");

        bookmarksScrollPane.setFadeScrollBars(false);
        bookmarksScrollPane.setScrollingDisabled(true, false);

        bookmarksScrollPane.setHeight(100 * GlobalConf.UI_SCALE_FACTOR);
        bookmarksScrollPane.setWidth(contentWidth);

        /*
         * ADD TO CONTENT
         */
        VerticalGroup objectsGroup = new VerticalGroup().align(Align.left).columnAlign(Align.left).space(space8);
        objectsGroup.addActor(searchBox);
        if (bookmarksScrollPane != null) {
            objectsGroup.addActor(bookmarksScrollPane);
        }
        objectsGroup.addActor(infoTable);

        component = objectsGroup;

    }

    private class TreeNode extends Tree.Node<TreeNode, String, OwnLabel> {
        public BNode node;

        public TreeNode(BNode node, Skin skin) {
            super(new OwnLabel(node.name, skin));
            this.node = node;
            setValue(node.name);
        }
    }

    public void reloadBookmarksTree() {
        java.util.List<BNode> bms = BookmarksManager.getBookmarks();
        bookmarksTree.clearChildren();
        for (BNode bookmark : bms) {
            TreeNode node = new TreeNode(bookmark, skin);
            if (bookmark.folder)
                node.setIcon(folderIcon);
            else
                node.setIcon(bookmarkIcon);
            bookmarksTree.add(node);
            genSubtree(node, bookmark);
        }
        bookmarksTree.pack();
    }

    private void genSubtree(TreeNode parent, BNode bookmark) {
        Drawable folderDrawable = skin.getDrawable("open");
        if (bookmark.children != null && !bookmark.children.isEmpty()) {
            for (BNode child : bookmark.children) {
                TreeNode tn = new TreeNode(child, skin);
                if (child.folder)
                    tn.setIcon(folderIcon);
                else
                    tn.setIcon(bookmarkIcon);
                parent.add(tn);
                genSubtree(tn, child);
            }
        }
    }

    public void selectBookmark(String bookmark, boolean fire) {
        if (bookmark != null || bookmarksTree.getSelectedValue() != bookmark) {
            boolean bkup = events;
            events = fire;
            // Select without firing events, do not use set()
            TreeNode node = bookmarksTree.findNode(bookmark);
            if (node != null) {
                bookmarksTree.getSelection().set(node);
                node.expandTo();
                scrollTo(node);
            }

            events = bkup;
        }
    }

    private void scrollTo(TreeNode node) {
        float y = getYPosition(bookmarksTree.getNodes(), node, 0f);
        bookmarksScrollPane.setScrollY(y);
    }

    private float getYPosition(Array<TreeNode> nodes, TreeNode node, float accumY) {
        if (nodes == null || nodes.isEmpty())
            return accumY;

        for (TreeNode n : nodes) {
            if (n != node) {
                accumY += n.getHeight() + bookmarksTree.getYSpacing();
                if (n.isExpanded()) {
                    accumY += getYPosition(n.getChildren(), node, 0f);
                }
            } else {
                // Found it!
                return accumY;
            }
            if (n.isAscendantOf(node))
                break;
        }
        return accumY;
    }

    public void setSceneGraph(ISceneGraph sg) {
        this.sg = sg;
    }

    private void info(String info1, String info2) {
        if (info1 == null) {
            infoMessage1.setText("");
            infoMessage2.setText("");
            info(false);
        } else {
            infoMessage1.setText(info1);
            infoMessage2.setText(info2);
            info(true);
        }
        infoTable.pack();
    }


    private void info(boolean visible) {
        if (visible) {
            infoCell1.setActor(infoMessage1);
            infoCell2.setActor(infoMessage2);
        } else {
            infoCell1.setActor(null);
            infoCell2.setActor(null);
        }
        infoTable.pack();
    }

    @Override
    public void notify(final Events event, final Object... data) {
        switch (event) {
            case FOCUS_CHANGED:
                // Update focus selection in focus list
                SceneGraphNode sgn;
                if (data[0] instanceof String) {
                    sgn = sg.getNode((String) data[0]);
                } else {
                    sgn = (SceneGraphNode) data[0];
                }
                // Select only if data[1] is true
                if (sgn != null) {
                    SceneGraphNode node = (SceneGraphNode) data[0];
                    selectBookmark(node.getName(), false);
                }
                break;
            case BOOKMARKS_ADD:
                String name = (String) data[0];
                reloadBookmarksTree();
                selectBookmark(name, false);
                break;
            case BOOKMARKS_REMOVE:
            case BOOKMARKS_REMOVE_ALL:
                reloadBookmarksTree();
                break;
            default:
                break;
        }

    }

    @Override
    public void dispose() {
        EventManager.instance.removeAllSubscriptions(this);
    }

}
