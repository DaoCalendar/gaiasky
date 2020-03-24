/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.interfce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import gaiasky.event.EventManager;
import gaiasky.event.Events;
import gaiasky.util.GlobalConf;
import gaiasky.util.I18n;
import gaiasky.util.scene2d.OwnCheckBox;
import gaiasky.util.scene2d.OwnLabel;

/**
 * GUI window to choose the catalogs to load by default.
 * This is shown at startup if no catalogs are selected and {@link GlobalConf#program#SKIP_CATALOG_CHOOSER} is false.
 * @author tsagrista
 *
 */
public class CatalogChooserWindow extends GenericDialog {

    private DatasetsWidget dw;
    private String assetsLoc;
    private String notice;
    private OwnCheckBox skipCatalogChooser;

    public CatalogChooserWindow(Stage stage, Skin skin){
        this(stage, skin, null);
    }

    public CatalogChooserWindow(Stage stage, Skin skin, String noticeKey) {
        super(I18n.txt("gui.dschooser.title"), skin, stage);
        this.notice = I18n.txt(noticeKey);
        assetsLoc = GlobalConf.ASSETS_LOC;

        setCancelText(I18n.txt("gui.exit"));
        setAcceptText(I18n.txt("gui.start"));

        // Build
        buildSuper();
    }

    @Override
    protected void build() {
        if(notice != null && !notice.isEmpty()){
            content.add(new OwnLabel(notice, skin)).left().pad(15 * GlobalConf.UI_SCALE_FACTOR).row();
        }

        Cell<Actor> cell = content.add((Actor) null);

        dw = new DatasetsWidget(skin, assetsLoc);
        Array<FileHandle> catalogFiles = dw.buildCatalogFiles();

        cell.clearActor();
        cell.space(3 * GlobalConf.UI_SCALE_FACTOR);
        cell.padTop(10 * GlobalConf.UI_SCALE_FACTOR);
        cell.setActor(dw.buildDatasetsWidget(catalogFiles));


        skipCatalogChooser = new OwnCheckBox(I18n.txt("gui.dschooser.notshow"), skin, pad5);
        skipCatalogChooser.setChecked(false);

        bottom.add(skipCatalogChooser).right().row();
    }

    @Override
    protected void accept() {
        GlobalConf.program.CATALOG_CHOOSER = skipCatalogChooser.isChecked() ? GlobalConf.ProgramConf.ShowCriterion.NEVER : GlobalConf.program.CATALOG_CHOOSER;
        // Update setting
        if (dw != null && dw.cbs != null) {
            GlobalConf.data.CATALOG_JSON_FILES.clear();
            for (Button b : dw.cbs) {
                if (b.isChecked()) {
                    // Add all selected to list
                    String candidate = dw.candidates.get(b);
                    GlobalConf.data.CATALOG_JSON_FILES.add(candidate);
                }
            }
        }
        // No change to execute exit event, manually restore cursor to default
        Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
        // Data load can start
        EventManager.instance.post(Events.LOAD_DATA_CMD);
    }

    @Override
    protected void cancel() {
        Gdx.app.exit();
    }

}