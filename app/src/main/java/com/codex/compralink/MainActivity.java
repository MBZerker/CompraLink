package com.codex.compralink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.content.res.Configuration;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class MainActivity extends Activity {
    private static final String PREFS = "compralink";
    private static final String KEY_LISTS = "lists";
    private static final String KEY_STOCK = "stock";
    private static final String KEY_SPENDING_HISTORY = "spending_history";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_ACCENT = "accent_color";
    private static final String KEY_LAST_CLIPBOARD_PAYLOAD = "last_clipboard_payload";
    private static final String KEY_MONTHLY_GOAL = "monthly_goal";
    private static final String KEY_SPENDING_RANGE = "spending_range_months";
    private static final String KEY_GAME_LEVEL = "market_game_level";
    private static final String KEY_GAME_MOVES = "market_game_moves";
    private static final String KEY_GAME_BOARD = "market_game_board";
    private static final String KEY_GAME_PLAYER_X = "market_game_player_x";
    private static final String KEY_GAME_PLAYER_Y = "market_game_player_y";
    private static final String KEY_GAME_BEST = "market_game_best";
    private static final int THEME_SYSTEM = 0;
    private static final int THEME_LIGHT = 1;
    private static final int THEME_DARK = 2;
    private static final String SHARE_BASE = "https://mbzerker.github.io/CompraLink/l/?payload=";
    private static final String PAGES_HOST = "mbzerker.github.io";
    private static final String PAGES_PATH = "/CompraLink/l/";
    private static final String OLD_SHARE_PREFIX = "https://compralink.app/list?payload=";
    private static final String CUSTOM_SHARE_PREFIX = "compralink://list?payload=";
    private static final int SORT_CHECKED_BOTTOM = 0;
    private static final int SORT_CHECKED_TOP = 1;
    private static final int SORT_KEEP_POSITION = 2;
    private static final String CUSTOM_CATEGORY = "Personalizada...";
    private static final long AUTO_LOCK_AFTER_MS = 24L * 60L * 60L * 1000L;

    private final List<ShoppingList> lists = new ArrayList<>();
    private final List<StockEntry> stock = new ArrayList<>();
    private final List<SpendingRecord> spendingHistory = new ArrayList<>();
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private LinearLayout root;
    private AutoCompleteTextView itemInput;
    private EditText priceInput;
    private EditText unitInput;
    private int selectedIndex = -1;
    private int homeTab = 0;
    private int spendingRangeMonths = 6;
    private double monthlyGoal;
    private boolean shellReady;
    private boolean pendingIntentHandled;
    private int themeMode = THEME_SYSTEM;
    private int accentColor = Color.rgb(15, 118, 110);
    private int secretLogoTaps;
    private long secretLastTap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeMode = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_THEME, THEME_SYSTEM);
        accentColor = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_ACCENT, Color.rgb(15, 118, 110));
        monthlyGoal = Double.longBitsToDouble(getSharedPreferences(PREFS, MODE_PRIVATE).getLong(KEY_MONTHLY_GOAL, Double.doubleToLongBits(0)));
        spendingRangeMonths = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_SPENDING_RANGE, 6);
        load();
        loadStock();
        loadSpendingHistory();
        ensureSpendingRecordsForClosedLists();
        showSplash();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            shellReady = true;
            handleIncomingIntent(getIntent());
            if (selectedIndex < 0) importClipboardListIfPresent();
            pendingIntentHandled = true;
            if (selectedIndex >= 0) {
                showListScreen();
            } else {
                showHomeScreen();
            }
            UpdateManager.checkForUpdates(this, false);
        }, 2000);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (shellReady) {
            handleIncomingIntent(intent);
            if (selectedIndex >= 0) showListScreen(); else showHomeScreen();
        }
    }

    @Override
    public void onBackPressed() {
        if (homeTab == 4) {
            showHomeScreen();
            return;
        }
        if (selectedIndex >= 0 || homeTab != 0) {
            selectedIndex = -1;
            homeTab = 0;
            showHomeScreen();
            return;
        }
        super.onBackPressed();
    }

    private void showSplash() {
        applySystemBars();
        LinearLayout splash = new LinearLayout(this);
        splash.setOrientation(LinearLayout.VERTICAL);
        splash.setGravity(Gravity.CENTER);
        splash.setPadding(dp(24), dp(24), dp(24), dp(24));
        splash.setBackgroundColor(screenBg());

        ImageView image = new ImageView(this);
        image.setImageResource(getResources().getIdentifier("splash_art", "drawable", getPackageName()));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        splash.addView(image, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        TextView name = new TextView(this);
        name.setText("CompraLink");
        name.setTextColor(accent());
        name.setTextSize(34);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setGravity(Gravity.CENTER);
        splash.addView(name, matchWrapWithTop(dp(18)));

        TextView tag = new TextView(this);
        tag.setText("listas, precos e compartilhamento");
        tag.setTextColor(mutedText());
        tag.setTextSize(15);
        tag.setGravity(Gravity.CENTER);
        splash.addView(tag, matchWrapWithTop(dp(4)));

        setContentView(splash);
    }

    private void showHomeScreen() {
        selectedIndex = -1;
        homeTab = 0;
        updateAutoLockedLists();
        buildRoot();
        addTopHeader("Suas listas", "Crie listas e compare precos salvos.", false);

        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).archived) continue;
            root.addView(listCard(i), matchWrapWithTop(dp(10)));
        }
        if (!hasVisibleLists(false)) {
            root.addView(infoCard("Nenhuma lista criada", "Toque no carrinho para criar sua primeira lista."), matchWrapWithTop(dp(10)));
        }
        setContentView(rootScroll());
    }

    private void showHistoryScreen() {
        selectedIndex = -1;
        homeTab = 3;
        updateAutoLockedLists();
        buildRoot();
        addTopHeader("Historico", "Listas protegidas ficam guardadas aqui.", false);
        addHistorySummary();

        for (int i = 0; i < lists.size(); i++) {
            if (!lists.get(i).archived) continue;
            root.addView(listCard(i), matchWrapWithTop(dp(10)));
        }
        if (!hasVisibleLists(true)) {
            root.addView(infoCard("Historico vazio", "Listas completas aparecem aqui depois de protegidas automaticamente."), matchWrapWithTop(dp(10)));
        }
        setContentView(rootScroll());
    }

    private void addHistorySummary() {
        int count = 0;
        int items = 0;
        double total = 0;
        for (ShoppingList list : lists) {
            if (!list.archived) continue;
            count++;
            items += list.items.size();
            for (ShoppingItem item : list.items) {
                if (item.price > 0) total += item.price * quantityOf(item);
            }
        }
        root.addView(infoCard("Resumo do historico", count + " listas, " + items + " itens, total " + money.format(total)), matchWrapWithTop(dp(10)));
    }

    private void showStockWindow(boolean spending) {
        selectedIndex = -1;
        homeTab = spending ? 2 : 1;
        buildRoot();
        addTopHeader("Estoque", spending ? "Gastos ficam dentro do estoque." : "Itens comprados e duracao estimada.", false);
        addStockTabs();
        if (spending) {
            addSpendingScreen();
        } else {
            addStockScreen();
        }
        setContentView(rootScroll());
    }

    private void showListScreen() {
        if (selectedIndex >= 0 && selectedIndex < lists.size()) {
            updateAutoLockedList(lists.get(selectedIndex));
        }
        buildRoot();
        ShoppingList list = lists.get(selectedIndex);
        addTopHeader(list.name, listSubtitle(list), true);
        if (list.locked) {
            root.addView(infoCard("Lista protegida", "Desbloqueie pelo cadeado para editar esta lista."), matchWrapWithTop(dp(10)));
        } else {
            addInputCard();
        }

        addItems();
        setContentView(rootScroll());
    }

    private void buildRoot() {
        applySystemBars();
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(22));
    }

    private ScrollView rootScroll() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(screenBg());
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return scrollView;
    }

    private void addTopHeader(String heading, String subheading, boolean listOpen) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(18), dp(18), dp(18));
        header.setBackground(round(cardBg(), dp(20), stroke(), 1));
        elevate(header, 3);
        root.addView(header, matchWrap());

        TextView appName = new TextView(this);
        appName.setText("CompraLink");
        appName.setTextColor(accent());
        appName.setTextSize(14);
        appName.setTypeface(Typeface.DEFAULT_BOLD);
        appName.setOnClickListener(v -> registerSecretLogoTap());
        header.addView(appName);

        TextView title = new TextView(this);
        title.setText(heading);
        title.setTextColor(primaryText());
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(6), 0, 0);
        header.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(subheading);
        subtitle.setTextColor(mutedText());
        subtitle.setTextSize(14);
        subtitle.setPadding(0, dp(4), 0, dp(14));
        header.addView(subtitle);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        header.addView(actions, matchWrap());

        if (listOpen) {
            Button back = button("Voltar", softButtonBg(), primaryText());
            back.setOnClickListener(v -> {
                selectedIndex = -1;
                showHomeScreen();
            });
            actions.addView(back, weighted());

            ImageButton share = imageIconButton(R.drawable.ic_share_nodes, Color.rgb(20, 184, 166), Color.WHITE);
            boolean canShare = selectedIndex >= 0 && !lists.get(selectedIndex).items.isEmpty();
            share.setEnabled(canShare);
            share.setAlpha(canShare ? 1.0f : 0.38f);
            share.setOnClickListener(v -> {
                if (canShare) shareSelectedList();
            });
            LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            shareParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(share, shareParams);

            ImageButton print = imageIconButton(R.drawable.ic_print, isDarkTheme() ? Color.rgb(71, 85, 105) : Color.rgb(51, 65, 85), Color.WHITE);
            print.setOnClickListener(v -> showPrintPreview());
            LinearLayout.LayoutParams printParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            printParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(print, printParams);

            ShoppingList current = lists.get(selectedIndex);
            ImageButton sort = imageIconButton(sortIcon(current.sortMode), softButtonBg(), primaryText());
            sort.setEnabled(!current.locked);
            sort.setAlpha(current.locked ? 0.38f : 1.0f);
            sort.setOnClickListener(v -> {
                if (current.locked) return;
                current.sortMode = (current.sortMode + 1) % 3;
                save();
                showListScreen();
            });
            LinearLayout.LayoutParams sortParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            sortParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(sort, sortParams);

            Button theme = iconButton(themeIcon(), isDarkTheme() ? Color.WHITE : Color.BLACK, isDarkTheme() ? Color.BLACK : Color.WHITE);
            theme.setOnClickListener(v -> toggleTheme());
            LinearLayout.LayoutParams themeParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            themeParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(theme, themeParams);

        } else {
            if (homeTab != 0) {
                Button back = button("Voltar", softButtonBg(), primaryText());
                back.setOnClickListener(v -> showHomeScreen());
                actions.addView(back, weighted());
            } else {
                ImageButton newList = imageIconButton(R.drawable.ic_cart, Color.rgb(15, 118, 110), Color.WHITE);
                newList.setOnClickListener(v -> promptNewList());
                actions.addView(newList, new LinearLayout.LayoutParams(dp(48), dp(48)));

                ImageButton stockButton = imageIconButton(R.drawable.ic_box, isDarkTheme() ? Color.rgb(71, 85, 105) : Color.rgb(51, 65, 85), Color.WHITE);
                stockButton.setOnClickListener(v -> showStockWindow(false));
                stockButton.setOnLongClickListener(v -> {
                    if (secretLogoTaps >= 3) {
                        showMarketGame();
                        return true;
                    }
                    return false;
                });
                LinearLayout.LayoutParams stockParams = new LinearLayout.LayoutParams(dp(48), dp(48));
                stockParams.setMargins(dp(8), 0, 0, 0);
                actions.addView(stockButton, stockParams);

                ImageButton history = imageIconButton(R.drawable.ic_history, isDarkTheme() ? Color.rgb(71, 85, 105) : Color.rgb(51, 65, 85), Color.WHITE);
                history.setOnClickListener(v -> showHistoryScreen());
                LinearLayout.LayoutParams historyParams = new LinearLayout.LayoutParams(dp(48), dp(48));
                historyParams.setMargins(dp(8), 0, 0, 0);
                actions.addView(history, historyParams);
            }

            if (homeTab == 0) {
                ImageButton update = imageIconButton(R.drawable.ic_update, isDarkTheme() ? Color.rgb(71, 85, 105) : Color.rgb(51, 65, 85), Color.WHITE);
                update.setOnClickListener(v -> UpdateManager.checkForUpdates(this, true));
                LinearLayout.LayoutParams updateParams = new LinearLayout.LayoutParams(dp(48), dp(48));
                updateParams.setMargins(dp(8), 0, 0, 0);
                actions.addView(update, updateParams);

                ImageButton backup = imageIconButton(R.drawable.ic_backup, isDarkTheme() ? Color.rgb(71, 85, 105) : Color.rgb(51, 65, 85), Color.WHITE);
                backup.setOnClickListener(v -> exportBackup());
                LinearLayout.LayoutParams backupParams = new LinearLayout.LayoutParams(dp(48), dp(48));
                backupParams.setMargins(dp(8), 0, 0, 0);
                actions.addView(backup, backupParams);

                ImageButton palette = imageIconButton(R.drawable.ic_palette, accentColor, isLightColor(accentColor) ? Color.rgb(15, 23, 42) : Color.WHITE);
                palette.setOnClickListener(v -> promptAccentColor());
                LinearLayout.LayoutParams paletteParams = new LinearLayout.LayoutParams(dp(48), dp(48));
                paletteParams.setMargins(dp(8), 0, 0, 0);
                actions.addView(palette, paletteParams);

                Button theme = iconButton(themeIcon(), isDarkTheme() ? Color.WHITE : Color.BLACK, isDarkTheme() ? Color.BLACK : Color.WHITE);
                theme.setOnClickListener(v -> toggleTheme());
                LinearLayout.LayoutParams themeParams = new LinearLayout.LayoutParams(dp(48), dp(48));
                themeParams.setMargins(dp(8), 0, 0, 0);
                actions.addView(theme, themeParams);
            }

        }
    }

    private void addStockTabs() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(0, dp(12), 0, 0);
        root.addView(tabs, matchWrap());
        addStockTabButton(tabs, "Estoque", false);
        addStockTabButton(tabs, "Gastos", true);
    }

    private void addStockTabButton(LinearLayout tabs, String label, boolean spending) {
        int tab = spending ? 2 : 1;
        Button btn = button(label, homeTab == tab ? accent() : softButtonBg(), homeTab == tab ? Color.WHITE : primaryText());
        btn.setTextSize(13);
        btn.setOnClickListener(v -> showStockWindow(spending));
        LinearLayout.LayoutParams params = weighted();
        if (tabs.getChildCount() > 0) params.setMargins(dp(6), 0, 0, 0);
        tabs.addView(btn, params);
    }

    private void registerSecretLogoTap() {
        long now = System.currentTimeMillis();
        if (now - secretLastTap > 1800) secretLogoTaps = 0;
        secretLastTap = now;
        secretLogoTaps++;
        if (secretLogoTaps == 3) {
            Toast.makeText(this, "Algo no estoque parece diferente...", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMarketGame() {
        selectedIndex = -1;
        homeTab = 4;
        secretLogoTaps = 0;
        buildRoot();
        addTopHeader("Mercado Geek", "Organize os produtos nas prateleiras certas.", false);

        MarketGeekView game = new MarketGeekView(this);
        TextView status = label(game.status(), 14, true, mutedText());
        status.setGravity(Gravity.CENTER);
        root.addView(status, matchWrapWithTop(dp(10)));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(actions, matchWrapWithTop(dp(10)));
        Button reset = button("Reiniciar", softButtonBg(), primaryText());
        Button scores = button("Recordes", softButtonBg(), primaryText());
        Button exit = button("Sair", softButtonBg(), primaryText());
        actions.addView(reset, weighted());
        LinearLayout.LayoutParams scoreParams = weighted();
        scoreParams.setMargins(dp(8), 0, dp(8), 0);
        actions.addView(scores, scoreParams);
        actions.addView(exit, weighted());

        root.addView(game, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(360)
        ));

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setGravity(Gravity.CENTER);
        root.addView(controls, matchWrapWithTop(dp(12)));

        Button up = iconButton("^", accent(), Color.WHITE);
        controls.addView(up, new LinearLayout.LayoutParams(dp(64), dp(52)));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        controls.addView(row, matchWrapWithTop(dp(6)));
        Button left = iconButton("<", accent(), Color.WHITE);
        Button down = iconButton("v", accent(), Color.WHITE);
        Button right = iconButton(">", accent(), Color.WHITE);
        row.addView(left, new LinearLayout.LayoutParams(dp(64), dp(52)));
        LinearLayout.LayoutParams mid = new LinearLayout.LayoutParams(dp(64), dp(52));
        mid.setMargins(dp(8), 0, dp(8), 0);
        row.addView(down, mid);
        row.addView(right, new LinearLayout.LayoutParams(dp(64), dp(52)));

        View.OnClickListener refresh = v -> status.setText(game.status());
        up.setOnClickListener(v -> {
            game.move(0, -1);
            refresh.onClick(v);
        });
        down.setOnClickListener(v -> {
            game.move(0, 1);
            refresh.onClick(v);
        });
        left.setOnClickListener(v -> {
            game.move(-1, 0);
            refresh.onClick(v);
        });
        right.setOnClickListener(v -> {
            game.move(1, 0);
            refresh.onClick(v);
        });
        reset.setOnClickListener(v -> {
            game.resetLevel();
            status.setText(game.status());
        });
        scores.setOnClickListener(v -> game.showBestScores());
        exit.setOnClickListener(v -> showHomeScreen());
        setContentView(rootScroll());
    }

    private View listCard(int index) {
        ShoppingList list = lists.get(index);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        int listColor = list.displayColor();
        card.setBackground(round(tintSurface(listColor), dp(16), listColor == 0 ? stroke() : listColor, 1));
        elevate(card, 3);
        card.setOnClickListener(v -> {
            selectedIndex = index;
            showListScreen();
        });
        card.setOnLongClickListener(v -> {
            showListOptions(index);
            return true;
        });

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(titleRow, matchWrap());

        TextView name = new TextView(this);
        name.setText(list.name);
        name.setTextSize(19);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setTextColor(listColor == 0 ? primaryText() : readableOnTint(listColor));
        titleRow.addView(name, weighted());

        ImageButton lock = imageIconButton(list.locked ? R.drawable.ic_lock_closed : R.drawable.ic_lock_open,
                list.locked ? Color.rgb(225, 29, 72) : Color.rgb(22, 163, 74),
                Color.WHITE);
        lock.setOnClickListener(v -> {
            toggleListLock(list);
            showHomeTab();
        });
        titleRow.addView(lock, new LinearLayout.LayoutParams(dp(42), dp(42)));

        TextView meta = new TextView(this);
        meta.setText(listSubtitle(list));
        meta.setTextSize(14);
        meta.setTextColor(mutedText());
        meta.setPadding(0, dp(5), 0, 0);
        card.addView(meta);
        return card;
    }

    private String listSubtitle(ShoppingList list) {
        int done = 0;
        double total = 0;
        for (ShoppingItem item : list.items) {
            if (item.checked) done++;
            if (item.price > 0) total += item.price * quantityOf(item);
        }
        String status = list.locked ? " - protegida" : "";
        return formatShortDate(list.createdAt) + " - " + list.items.size() + " itens, " + done + " concluidos, total " + money.format(total) + status;
    }

    private void showHomeTab() {
        if (homeTab == 3) {
            showHistoryScreen();
        } else {
            showHomeScreen();
        }
    }

    private boolean hasVisibleLists(boolean archived) {
        for (ShoppingList list : lists) {
            if (list.archived == archived) return true;
        }
        return false;
    }

    private void toggleListLock(ShoppingList list) {
        if (list.locked) {
            list.locked = false;
            list.archived = false;
            list.lockedAt = 0;
        } else {
            list.locked = true;
            list.lockedAt = System.currentTimeMillis();
            addSpendingRecordsForList(list);
        }
        save();
    }

    private void updateAutoLockedLists() {
        boolean changed = false;
        for (ShoppingList list : lists) {
            changed = updateAutoLockedList(list) || changed;
        }
        if (changed) save();
    }

    private boolean updateAutoLockedList(ShoppingList list) {
        if (list.locked || list.items.isEmpty()) return false;
        if (System.currentTimeMillis() - list.createdAt < AUTO_LOCK_AFTER_MS) return false;
        for (ShoppingItem item : list.items) {
            if (!item.checked) return false;
        }
        list.locked = true;
        list.archived = true;
        list.lockedAt = System.currentTimeMillis();
        addSpendingRecordsForList(list);
        return true;
    }

    private void showPrintPreview() {
        if (selectedIndex < 0) return;
        ShoppingList list = lists.get(selectedIndex);
        applySystemBars();

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setPadding(dp(14), previewTopPadding(), dp(14), dp(18));
        screen.setBackgroundColor(screenBg());

        addPrintPreviewToolbar(screen, () -> showListScreen(), () -> printHtml("Lista - " + list.name, buildListPrintHtml(list)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(28), dp(32), dp(28), dp(32));
        page.setBackgroundColor(Color.WHITE);
        int pageWidth = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), dp(420));
        int pageHeight = (int) (pageWidth * 1.414f);
        LinearLayout.LayoutParams pageParams = new LinearLayout.LayoutParams(pageWidth, pageHeight);
        pageParams.gravity = Gravity.CENTER_HORIZONTAL;
        pageParams.setMargins(0, dp(12), 0, dp(20));

        TextView title = printText(list.name, 22, true);
        title.setGravity(Gravity.CENTER);
        page.addView(title, matchWrap());

        TextView spacer = printText("\n", 14, false);
        page.addView(spacer, matchWrap());

        for (ShoppingItem item : list.items) {
            double qty = quantityOf(item);
            String unitPrice = item.price > 0 ? money.format(item.price) : "R$ --";
            String total = item.price > 0 ? money.format(item.price * qty) : "R$ --";
            TextView line = printText("• " + item.name + "\n  " + formatQty(qty) + " x " + unitPrice + " (" + total + ")", 15, false);
            line.setPadding(0, 0, 0, dp(10));
            page.addView(line, matchWrap());
        }

        scroll.addView(page, pageParams);
        screen.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(screen);
    }

    private void addPrintPreviewToolbar(LinearLayout screen, Runnable closeAction, Runnable printAction) {
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);

        Button close = iconButton("X", softButtonBg(), primaryText());
        close.setTextSize(18);
        close.setOnClickListener(v -> closeAction.run());
        toolbar.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));

        View spacer = new View(this);
        toolbar.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));

        ImageButton print = imageIconButton(R.drawable.ic_print, isDarkTheme() ? Color.rgb(71, 85, 105) : Color.rgb(51, 65, 85), Color.WHITE);
        print.setOnClickListener(v -> printAction.run());
        toolbar.addView(print, new LinearLayout.LayoutParams(dp(48), dp(48)));

        screen.addView(toolbar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
    }

    private int previewTopPadding() {
        return dp(14) + statusBarHeight();
    }

    private int statusBarHeight() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) return getResources().getDimensionPixelSize(id);
        return dp(24);
    }

    private void printHtml(String jobName, String html) {
        PrintManager manager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        if (manager == null) {
            Toast.makeText(this, "Impressao indisponivel neste aparelho.", Toast.LENGTH_SHORT).show();
            return;
        }
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                PrintDocumentAdapter adapter = view.createPrintDocumentAdapter(jobName);
                PrintAttributes attributes = new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build();
                manager.print(jobName, adapter, attributes);
            }
        });
        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null);
    }

    private String buildListPrintHtml(ShoppingList list) {
        StringBuilder html = printHtmlStart(list.name);
        html.append("<h1>").append(escapeHtml(list.name)).append("</h1><div class=\"gap\"></div>");
        for (ShoppingItem item : list.items) {
            double qty = quantityOf(item);
            String unitPrice = item.price > 0 ? money.format(item.price) : "R$ --";
            String total = item.price > 0 ? money.format(item.price * qty) : "R$ --";
            html.append("<p class=\"item\">&bull; ")
                    .append(escapeHtml(item.name))
                    .append("<br><span>")
                    .append(escapeHtml(formatQty(qty)))
                    .append(" x ")
                    .append(escapeHtml(unitPrice))
                    .append(" (")
                    .append(escapeHtml(total))
                    .append(")</span></p>");
        }
        return html.append("</body></html>").toString();
    }

    private TextView printText(String text, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(Color.BLACK);
        view.setLineSpacing(dp(2), 1.0f);
        if (bold) view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    private void showSpendingReportPreview() {
        applySystemBars();
        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setPadding(dp(14), previewTopPadding(), dp(14), dp(18));
        screen.setBackgroundColor(screenBg());

        addPrintPreviewToolbar(screen, () -> showStockWindow(true), () -> printHtml("Relatorio de gastos", buildSpendingReportHtml()));

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(28), dp(32), dp(28), dp(32));
        page.setBackgroundColor(Color.WHITE);
        int pageWidth = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), dp(420));
        int pageHeight = (int) (pageWidth * 1.414f);
        LinearLayout.LayoutParams pageParams = new LinearLayout.LayoutParams(pageWidth, pageHeight);
        pageParams.gravity = Gravity.CENTER_HORIZONTAL;
        pageParams.setMargins(0, dp(12), 0, dp(20));

        TextView title = printText("Relatório de gastos", 22, true);
        title.setGravity(Gravity.CENTER);
        page.addView(title, matchWrap());
        page.addView(printText("Período: " + spendingRangeLabel(), 14, false), matchWrapWithTop(dp(12)));
        page.addView(printText("Gerado em: " + formatDateTime(System.currentTimeMillis()), 14, false), matchWrapWithTop(dp(2)));

        Map<String, Double> categories = new LinkedHashMap<>();
        Map<String, SpendingProduct> products = new LinkedHashMap<>();
        double total = 0;
        for (SpendingRecord entry : spendingHistory) {
            if (entry.price <= 0 || !entryInSelectedRange(entry)) continue;
            double value = entry.price * entry.quantity;
            total += value;
            String category = categoryOf(entry);
            categories.put(category, categories.containsKey(category) ? categories.get(category) + value : value);
            String key = normalize(entry.name);
            SpendingProduct product = products.get(key);
            if (product == null) {
                product = new SpendingProduct(entry.name);
                products.put(key, product);
            }
            product.total += value;
            product.quantity += entry.quantity;
            product.times += 1;
        }

        page.addView(printText("\nResumo", 18, true), matchWrap());
        page.addView(printText("Total: " + money.format(total), 15, true), matchWrapWithTop(dp(6)));
        if (monthlyGoal > 0) {
            page.addView(printText("Meta mensal: " + money.format(monthlyGoal), 14, false), matchWrapWithTop(dp(2)));
            page.addView(printText("Saldo da meta: " + money.format(monthlyGoal - total), 14, false), matchWrapWithTop(dp(2)));
        }

        page.addView(printText("\nCategorias", 18, true), matchWrap());
        List<Map.Entry<String, Double>> categoryRows = new ArrayList<>(categories.entrySet());
        Collections.sort(categoryRows, (a, b) -> Double.compare(b.getValue(), a.getValue()));
        if (categoryRows.isEmpty()) {
            page.addView(printText("Sem dados no período.", 14, false), matchWrapWithTop(dp(4)));
        } else {
            for (Map.Entry<String, Double> row : categoryRows) {
                page.addView(printText("• " + row.getKey() + ": " + money.format(row.getValue()), 14, false), matchWrapWithTop(dp(4)));
            }
        }

        page.addView(printText("\nProdutos", 18, true), matchWrap());
        List<SpendingProduct> productRows = new ArrayList<>(products.values());
        Collections.sort(productRows, (a, b) -> Double.compare(b.total, a.total));
        int limit = Math.min(10, productRows.size());
        if (limit == 0) {
            page.addView(printText("Sem dados no período.", 14, false), matchWrapWithTop(dp(4)));
        }
        for (int i = 0; i < limit; i++) {
            SpendingProduct product = productRows.get(i);
            page.addView(printText((i + 1) + ". " + product.name + " - " + money.format(product.total)
                    + " - " + formatQty(product.quantity) + " un", 14, false), matchWrapWithTop(dp(4)));
        }

        scroll.addView(page, pageParams);
        screen.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(screen);
    }

    private String buildSpendingReportHtml() {
        Map<String, Double> categories = new LinkedHashMap<>();
        Map<String, SpendingProduct> products = new LinkedHashMap<>();
        double total = 0;
        for (SpendingRecord entry : spendingHistory) {
            if (entry.price <= 0 || !entryInSelectedRange(entry)) continue;
            double value = entry.price * entry.quantity;
            total += value;
            String category = categoryOf(entry);
            categories.put(category, categories.containsKey(category) ? categories.get(category) + value : value);
            String key = normalize(entry.name);
            SpendingProduct product = products.get(key);
            if (product == null) {
                product = new SpendingProduct(entry.name);
                products.put(key, product);
            }
            product.total += value;
            product.quantity += entry.quantity;
            product.times += 1;
        }

        StringBuilder html = printHtmlStart("Relatório de gastos");
        html.append("<h1>Relatório de gastos</h1>");
        html.append("<p>Período: ").append(escapeHtml(spendingRangeLabel())).append("</p>");
        html.append("<p>Gerado em: ").append(escapeHtml(formatDateTime(System.currentTimeMillis()))).append("</p>");
        html.append("<h2>Resumo</h2>");
        html.append("<p><strong>Total: ").append(escapeHtml(money.format(total))).append("</strong></p>");
        if (monthlyGoal > 0) {
            html.append("<p>Meta mensal: ").append(escapeHtml(money.format(monthlyGoal))).append("</p>");
            html.append("<p>Saldo da meta: ").append(escapeHtml(money.format(monthlyGoal - total))).append("</p>");
        }

        html.append("<h2>Categorias</h2>");
        List<Map.Entry<String, Double>> categoryRows = new ArrayList<>(categories.entrySet());
        Collections.sort(categoryRows, (a, b) -> Double.compare(b.getValue(), a.getValue()));
        if (categoryRows.isEmpty()) {
            html.append("<p>Sem dados no período.</p>");
        } else {
            for (Map.Entry<String, Double> row : categoryRows) {
                html.append("<p class=\"item\">&bull; ")
                        .append(escapeHtml(row.getKey()))
                        .append(": ")
                        .append(escapeHtml(money.format(row.getValue())))
                        .append("</p>");
            }
        }

        html.append("<h2>Produtos</h2>");
        List<SpendingProduct> productRows = new ArrayList<>(products.values());
        Collections.sort(productRows, (a, b) -> Double.compare(b.total, a.total));
        int limit = Math.min(10, productRows.size());
        if (limit == 0) {
            html.append("<p>Sem dados no período.</p>");
        }
        for (int i = 0; i < limit; i++) {
            SpendingProduct product = productRows.get(i);
            html.append("<p>")
                    .append(i + 1)
                    .append(". ")
                    .append(escapeHtml(product.name))
                    .append(" - ")
                    .append(escapeHtml(money.format(product.total)))
                    .append(" - ")
                    .append(escapeHtml(formatQty(product.quantity)))
                    .append(" un</p>");
        }
        return html.append("</body></html>").toString();
    }

    private StringBuilder printHtmlStart(String title) {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>")
                .append(escapeHtml(title))
                .append("</title><style>")
                .append("@page{size:A4;margin:18mm;}body{font-family:Arial,sans-serif;color:#000;background:#fff;font-size:14pt;line-height:1.35;}")
                .append("h1{text-align:center;font-size:22pt;margin:0 0 18pt;}h2{font-size:17pt;margin:20pt 0 8pt;}")
                .append("p{margin:4pt 0;}.gap{height:18pt;}.item{margin:0 0 10pt;}span{font-size:13pt;}")
                .append("</style></head><body>");
        return html;
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void addStockScreen() {
        if (stock.isEmpty()) {
            root.addView(infoCard("Estoque vazio", "Marque itens comprados nas listas para adiciona-los ao estoque."), matchWrapWithTop(dp(10)));
            return;
        }
        for (StockEntry entry : stock) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));
            card.setBackground(round(cardBg(), dp(16), stroke(), 1));
            card.setOnLongClickListener(v -> {
                showStockOptions(entry);
                return true;
            });
            TextView name = label(entry.name, 18, true, primaryText());
            card.addView(name);
            String price = entry.price > 0 ? money.format(entry.price) : "sem preco";
            String total = entry.price > 0 ? money.format(entry.price * entry.quantity) : "sem preco";
            TextView meta = label(formatQty(entry.quantity) + " x " + price + " (" + total + ")", 14, true, mutedText());
            meta.setPadding(0, dp(4), 0, 0);
            card.addView(meta);
            TextView duration = label(formatStockAge(entry), 14, false, accent());
            duration.setPadding(0, dp(5), 0, 0);
            card.addView(duration);
            TextView category = label("Categoria: " + categoryOf(entry), 13, false, mutedText());
            category.setPadding(0, dp(4), 0, 0);
            card.addView(category);
            TextView edited = label("Editado: " + formatDateTime(entry.updatedAt), 13, false, mutedText());
            edited.setPadding(0, dp(4), 0, 0);
            card.addView(edited);
            root.addView(card, matchWrapWithTop(dp(10)));
        }
    }

    private void addSpendingScreen() {
        Map<String, Double> totals = new LinkedHashMap<>();
        Map<String, SpendingProduct> products = new LinkedHashMap<>();
        Map<String, Double> categories = new LinkedHashMap<>();
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        Calendar currentMonth = Calendar.getInstance();
        Calendar previousMonth = Calendar.getInstance();
        previousMonth.add(Calendar.MONTH, -1);
        double currentTotal = 0;
        double previousTotal = 0;
        SpendingRecord biggestEntry = null;
        int visibleMonths = spendingRangeMonths <= 0 ? Math.max(6, countMonthsWithData()) : spendingRangeMonths;
        for (int i = visibleMonths - 1; i >= 0; i--) {
            cal.setTimeInMillis(now);
            cal.add(Calendar.MONTH, -i);
            totals.put(monthKey(cal), 0.0);
        }
        for (SpendingRecord entry : spendingHistory) {
            if (entry.price <= 0) continue;
            double total = entry.price * entry.quantity;
            cal.setTimeInMillis(entry.addedAt);
            String key = monthKey(cal);
            boolean inSelectedRange = spendingRangeMonths <= 0 || totals.containsKey(key);
            if (totals.containsKey(key)) {
                totals.put(key, totals.get(key) + total);
            }
            if (sameMonth(cal, currentMonth)) currentTotal += total;
            if (sameMonth(cal, previousMonth)) previousTotal += total;
            if (biggestEntry == null || total > biggestEntry.price * biggestEntry.quantity) biggestEntry = entry;
            if (!inSelectedRange) continue;
            String category = categoryOf(entry);
            categories.put(category, categories.containsKey(category) ? categories.get(category) + total : total);

            String productKey = normalize(entry.name);
            SpendingProduct product = products.get(productKey);
            if (product == null) {
                product = new SpendingProduct(entry.name);
                products.put(productKey, product);
            }
            product.total += total;
            product.quantity += entry.quantity;
            product.times += 1;
            product.priceSum += entry.price;
            if (product.times == 1 || entry.price < product.minPrice) product.minPrice = entry.price;
            if (product.times == 1 || entry.price > product.maxPrice) product.maxPrice = entry.price;
            if (entry.addedAt >= product.latestAt) {
                product.latestAt = entry.addedAt;
                product.latestPrice = entry.price;
            }
        }
        double max = 1;
        double sum = 0;
        for (double value : totals.values()) {
            max = Math.max(max, value);
            sum += value;
        }
        double average = totals.isEmpty() ? 0 : sum / totals.size();
        double difference = currentTotal - previousTotal;
        double forecast = forecastMonthTotal(currentTotal);

        addSpendingFilters();
        root.addView(infoCard("Resumo", "Total do período selecionado: " + money.format(sum)), matchWrapWithTop(dp(10)));
        addMetricGrid(currentTotal, previousTotal, difference, average, forecast, biggestEntry, products);
        addGoalCard(currentTotal, forecast);
        addSpendingAlerts(currentTotal, forecast, products);
        addMonthlyBars(totals, max);
        addCategoryBreakdown(categories);
        addProductRanking(products);
        addPriceInsights(products);
    }

    private void addSpendingFilters() {
        if (System.currentTimeMillis() >= 0) {
            addCompactSpendingFilters();
            return;
        }
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row, matchWrapWithTop(dp(10)));
        addSpendingFilterButton(row, "1m", 1);
        addSpendingFilterButton(row, "3m", 3);
        addSpendingFilterButton(row, "6m", 6);
        addSpendingFilterButton(row, "12m", 12);
        addSpendingFilterButton(row, "Tudo", 0);
        addGoalButton();
        Button report = button("Relatório", Color.rgb(51, 65, 85), Color.WHITE);
        report.setOnClickListener(v -> showSpendingReportPreview());
        root.addView(report, matchWrapWithTop(dp(8)));
    }

    private void addGoalButton() {
        Button goal = button("Meta", monthlyGoal > 0 ? Color.rgb(22, 163, 74) : softButtonBg(), monthlyGoal > 0 ? Color.WHITE : primaryText());
        goal.setOnClickListener(v -> promptMonthlyGoal());
        root.addView(goal, matchWrapWithTop(dp(8)));
    }

    private void addSpendingFilterButton(LinearLayout row, String label, int months) {
        Button button = button(label, spendingRangeMonths == months ? accent() : softButtonBg(), spendingRangeMonths == months ? Color.WHITE : primaryText());
        button.setTextSize(12);
        button.setOnClickListener(v -> {
            spendingRangeMonths = months;
            saveSpendingRange();
            showStockWindow(true);
        });
        LinearLayout.LayoutParams params = weighted();
        if (row.getChildCount() > 0) params.setMargins(dp(5), 0, 0, 0);
        row.addView(button, params);
    }

    private void addCompactSpendingFilters() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(round(cardBg(), dp(16), stroke(), 1));
        root.addView(card, matchWrapWithTop(dp(10)));

        Spinner range = new Spinner(this);
        String[] labels = new String[]{"Mes atual", "Ultimos 3 meses", "Ultimos 6 meses", "Ultimos 12 meses", "Tudo"};
        int[] values = new int[]{1, 3, 6, 12, 0};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        range.setAdapter(adapter);
        range.setSelection(spendingRangeIndex(values));
        range.setBackground(round(inputBg(), dp(12), stroke(), 1));
        range.setPadding(dp(8), 0, dp(8), 0);
        range.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            boolean ready;

            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(primaryText());
                    ((TextView) view).setTextSize(14);
                }
                if (!ready) {
                    ready = true;
                    return;
                }
                spendingRangeMonths = values[position];
                saveSpendingRange();
                showStockWindow(true);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        card.addView(range, new LinearLayout.LayoutParams(0, dp(48), 1));

        ImageButton goal = imageIconButton(R.drawable.ic_target, monthlyGoal > 0 ? Color.rgb(22, 163, 74) : softButtonBg(), monthlyGoal > 0 ? Color.WHITE : primaryText());
        goal.setOnClickListener(v -> promptMonthlyGoal());
        LinearLayout.LayoutParams goalParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        goalParams.setMargins(dp(8), 0, 0, 0);
        card.addView(goal, goalParams);

        ImageButton report = imageIconButton(R.drawable.ic_report, Color.rgb(51, 65, 85), Color.WHITE);
        report.setOnClickListener(v -> showSpendingReportPreview());
        LinearLayout.LayoutParams reportParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        reportParams.setMargins(dp(8), 0, 0, 0);
        card.addView(report, reportParams);

        ImageButton clear = imageIconButton(R.drawable.ic_trash, Color.rgb(225, 29, 72), Color.WHITE);
        clear.setOnClickListener(v -> confirmClearSpendingHistory());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        clearParams.setMargins(dp(8), 0, 0, 0);
        card.addView(clear, clearParams);
    }

    private int spendingRangeIndex(int[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == spendingRangeMonths) return i;
        }
        return 2;
    }

    private void saveSpendingRange() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putInt(KEY_SPENDING_RANGE, spendingRangeMonths)
                .apply();
    }

    private void confirmClearSpendingHistory() {
        dialog()
                .setTitle("Limpar gastos?")
                .setMessage("Deseja mesmo limpar todo o historico de gastos usado nos calculos e relatorios?")
                .setPositiveButton("Continuar", (dialog, which) -> confirmClearSpendingHistoryForever())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmClearSpendingHistoryForever() {
        dialog()
                .setTitle("Acao permanente")
                .setMessage("Isso apagará o historico de gastos e nao podera ser restaurado pelo app. Listas e estoque nao serao apagados. Continuar?")
                .setPositiveButton("Limpar", (dialog, which) -> {
                    spendingHistory.clear();
                    saveSpendingHistory();
                    Toast.makeText(this, "Historico de gastos limpo.", Toast.LENGTH_SHORT).show();
                    showStockWindow(true);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void exportBackup() {
        try {
            String backup = buildBackupJson().toString(2);
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("application/json");
            send.putExtra(Intent.EXTRA_SUBJECT, "Backup CompraLink");
            send.putExtra(Intent.EXTRA_TEXT, backup);
            startActivity(Intent.createChooser(send, "Exportar backup"));
        } catch (Exception e) {
            Toast.makeText(this, "Nao foi possivel gerar o backup.", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject buildBackupJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("app", "CompraLink");
        json.put("backupVersion", 1);
        json.put("createdAt", System.currentTimeMillis());

        JSONArray listArray = new JSONArray();
        for (ShoppingList list : lists) listArray.put(list.toJson());
        json.put("lists", listArray);

        JSONArray stockArray = new JSONArray();
        for (StockEntry entry : stock) stockArray.put(entry.toJson());
        json.put("stock", stockArray);

        JSONArray spendingArray = new JSONArray();
        for (SpendingRecord entry : spendingHistory) spendingArray.put(entry.toJson());
        json.put("spendingHistory", spendingArray);

        JSONObject settings = new JSONObject();
        settings.put("themeMode", themeMode);
        settings.put("accentColor", accentColor);
        settings.put("spendingRangeMonths", spendingRangeMonths);
        settings.put("monthlyGoal", monthlyGoal);
        settings.put("gameBest", getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_GAME_BEST, ""));
        json.put("settings", settings);
        return json;
    }

    private void addMetricGrid(double currentTotal, double previousTotal, double difference, double average, double forecast, SpendingRecord biggestEntry, Map<String, SpendingProduct> products) {
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row1, matchWrapWithTop(dp(8)));
        row1.addView(metricCard("Mês atual", money.format(currentTotal), accent()), weighted());
        LinearLayout.LayoutParams right = weighted();
        right.setMargins(dp(8), 0, 0, 0);
        row1.addView(metricCard("Mês anterior", money.format(previousTotal), primaryText()), right);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row2, matchWrapWithTop(dp(8)));
        int diffColor = difference <= 0 ? Color.rgb(22, 163, 74) : Color.rgb(225, 29, 72);
        row2.addView(metricCard("Diferença", money.format(difference), diffColor), weighted());
        LinearLayout.LayoutParams avgParams = weighted();
        avgParams.setMargins(dp(8), 0, 0, 0);
        row2.addView(metricCard("Média mensal", money.format(average), primaryText()), avgParams);

        LinearLayout rowForecast = new LinearLayout(this);
        rowForecast.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(rowForecast, matchWrapWithTop(dp(8)));
        rowForecast.addView(metricCard("Previsão do mês", money.format(forecast), forecast > previousTotal && previousTotal > 0 ? Color.rgb(225, 29, 72) : accent()), weighted());
        LinearLayout.LayoutParams paceParams = weighted();
        paceParams.setMargins(dp(8), 0, 0, 0);
        String pace = previousTotal <= 0 ? "Sem comparação" : (forecast > previousTotal ? "Acima do mês anterior" : "Dentro do ritmo");
        rowForecast.addView(metricCard("Ritmo", pace, primaryText()), paceParams);

        SpendingProduct mostBought = mostBoughtProduct(products);
        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row3, matchWrapWithTop(dp(8)));
        String biggest = biggestEntry == null ? "Sem dados" : biggestEntry.name + " - " + money.format(biggestEntry.price * biggestEntry.quantity);
        row3.addView(metricCard("Maior compra", biggest, primaryText()), weighted());
        LinearLayout.LayoutParams boughtParams = weighted();
        boughtParams.setMargins(dp(8), 0, 0, 0);
        String bought = mostBought == null ? "Sem dados" : mostBought.name + " - " + formatQty(mostBought.quantity) + " un";
        row3.addView(metricCard("Mais comprado", bought, primaryText()), boughtParams);
    }

    private View metricCard(String title, String value, int valueColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(round(cardBg(), dp(14), stroke(), 1));
        elevate(card, 2);
        card.addView(label(title, 12, true, mutedText()));
        TextView valueView = label(value, 16, true, valueColor);
        valueView.setPadding(0, dp(5), 0, 0);
        valueView.setSingleLine(false);
        card.addView(valueView, matchWrap());
        return card;
    }

    private void addGoalCard(double currentTotal, double forecast) {
        if (monthlyGoal <= 0) {
            root.addView(infoCard("Meta mensal", "Toque em Meta para definir um limite mensal de gastos."), matchWrapWithTop(dp(10)));
            return;
        }
        double remaining = monthlyGoal - currentTotal;
        double percent = monthlyGoal <= 0 ? 0 : currentTotal / monthlyGoal;
        int statusColor = remaining >= 0 ? Color.rgb(22, 163, 74) : Color.rgb(225, 29, 72);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(round(cardBg(), dp(14), stroke(), 1));
        card.addView(label("Meta mensal", 18, true, primaryText()));
        TextView text = label("Usado: " + money.format(currentTotal)
                + " de " + money.format(monthlyGoal)
                + " - " + (remaining >= 0 ? "restam " : "passou ")
                + money.format(Math.abs(remaining)), 14, true, statusColor);
        text.setPadding(0, dp(6), 0, 0);
        card.addView(text, matchWrap());
        TextView forecastText = label("Previsão: " + money.format(forecast), 13, false, mutedText());
        forecastText.setPadding(0, dp(4), 0, 0);
        card.addView(forecastText, matchWrap());
        LinearLayout barBg = new LinearLayout(this);
        barBg.setBackground(round(inputBg(), dp(8), Color.TRANSPARENT, 0));
        LinearLayout bar = new LinearLayout(this);
        bar.setBackground(round(statusColor, dp(8), Color.TRANSPARENT, 0));
        int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.72);
        int width = Math.max(dp(8), (int) (maxWidth * Math.min(1.0, percent)));
        barBg.addView(bar, new LinearLayout.LayoutParams(width, dp(14)));
        card.addView(barBg, matchWrapWithTop(dp(8)));
        root.addView(card, matchWrapWithTop(dp(10)));
    }

    private void addSpendingAlerts(double currentTotal, double forecast, Map<String, SpendingProduct> products) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(round(cardBg(), dp(14), stroke(), 1));
        card.addView(label("Alertas", 18, true, primaryText()));
        int count = 0;
        if (monthlyGoal > 0 && currentTotal > monthlyGoal) {
            addAlertLine(card, "Meta mensal ultrapassada em " + money.format(currentTotal - monthlyGoal), Color.rgb(225, 29, 72));
            count++;
        } else if (monthlyGoal > 0 && forecast > monthlyGoal) {
            addAlertLine(card, "Previsão acima da meta em " + money.format(forecast - monthlyGoal), Color.rgb(234, 88, 12));
            count++;
        }
        List<SpendingProduct> expensive = new ArrayList<>();
        for (SpendingProduct product : products.values()) {
            if (product.times >= 2 && product.latestPrice > product.priceSum / product.times) expensive.add(product);
        }
        Collections.sort(expensive, (a, b) -> Double.compare(
                (b.latestPrice - (b.priceSum / b.times)),
                (a.latestPrice - (a.priceSum / a.times))));
        int limit = Math.min(3, expensive.size());
        for (int i = 0; i < limit; i++) {
            SpendingProduct product = expensive.get(i);
            double average = product.priceSum / product.times;
            addAlertLine(card, product.name + " acima da média em " + money.format(product.latestPrice - average), Color.rgb(234, 88, 12));
            count++;
        }
        if (count == 0) {
            addAlertLine(card, "Nenhum alerta importante no período selecionado.", Color.rgb(22, 163, 74));
        }
        root.addView(card, matchWrapWithTop(dp(10)));
    }

    private void addAlertLine(LinearLayout card, String text, int color) {
        TextView line = label(text, 14, true, color);
        line.setPadding(0, dp(6), 0, 0);
        card.addView(line, matchWrap());
    }

    private void promptMonthlyGoal() {
        EditText input = dialogInput("Meta mensal", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (monthlyGoal > 0) {
            input.setText(formatPriceInput(monthlyGoal));
            input.setSelection(input.getText().length());
        }
        dialog()
                .setTitle("Meta mensal")
                .setView(input)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    monthlyGoal = parsePrice(input.getText().toString());
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                            .putLong(KEY_MONTHLY_GOAL, Double.doubleToLongBits(monthlyGoal))
                            .apply();
                    showStockWindow(true);
                })
                .setNeutralButton("Remover meta", (dialog, which) -> {
                    monthlyGoal = 0;
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                            .putLong(KEY_MONTHLY_GOAL, Double.doubleToLongBits(0))
                            .apply();
                    showStockWindow(true);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void addMonthlyBars(Map<String, Double> totals, double max) {
        root.addView(label("Gastos por mês", 18, true, primaryText()), matchWrapWithTop(dp(16)));
        for (String key : totals.keySet()) {
            double value = totals.get(key);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackground(round(cardBg(), dp(14), stroke(), 1));
            card.addView(label(key + " · " + money.format(value), 15, true, primaryText()));
            LinearLayout barBg = new LinearLayout(this);
            barBg.setPadding(0, 0, 0, 0);
            barBg.setBackground(round(inputBg(), dp(8), Color.TRANSPARENT, 0));
            LinearLayout bar = new LinearLayout(this);
            bar.setBackground(round(accent(), dp(8), Color.TRANSPARENT, 0));
            int width = Math.max(dp(8), (int) (getResources().getDisplayMetrics().widthPixels * 0.72 * (value / max)));
            barBg.addView(bar, new LinearLayout.LayoutParams(width, dp(14)));
            card.addView(barBg, matchWrapWithTop(dp(8)));
            root.addView(card, matchWrapWithTop(dp(8)));
        }
    }

    private void addCategoryBreakdown(Map<String, Double> categories) {
        List<Map.Entry<String, Double>> ranking = new ArrayList<>(categories.entrySet());
        Collections.sort(ranking, (a, b) -> Double.compare(b.getValue(), a.getValue()));
        root.addView(label("Gastos por categoria", 18, true, primaryText()), matchWrapWithTop(dp(16)));
        if (ranking.isEmpty()) {
            root.addView(infoCard("Sem categorias", "Edite a categoria dos itens no estoque para analisar seus gastos por grupo."), matchWrapWithTop(dp(8)));
            return;
        }
        double max = Math.max(1, ranking.get(0).getValue());
        for (Map.Entry<String, Double> entry : ranking) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackground(round(cardBg(), dp(14), stroke(), 1));
            card.addView(label(entry.getKey() + " - " + money.format(entry.getValue()), 15, true, primaryText()));
            LinearLayout barBg = new LinearLayout(this);
            barBg.setBackground(round(inputBg(), dp(8), Color.TRANSPARENT, 0));
            LinearLayout bar = new LinearLayout(this);
            bar.setBackground(round(categoryColor(entry.getKey()), dp(8), Color.TRANSPARENT, 0));
            int width = Math.max(dp(8), (int) (getResources().getDisplayMetrics().widthPixels * 0.72 * (entry.getValue() / max)));
            barBg.addView(bar, new LinearLayout.LayoutParams(width, dp(12)));
            card.addView(barBg, matchWrapWithTop(dp(8)));
            root.addView(card, matchWrapWithTop(dp(8)));
        }
    }

    private void addProductRanking(Map<String, SpendingProduct> products) {
        List<SpendingProduct> ranking = new ArrayList<>(products.values());
        Collections.sort(ranking, (a, b) -> Double.compare(b.total, a.total));
        root.addView(label("Produtos que mais pesam", 18, true, primaryText()), matchWrapWithTop(dp(16)));
        if (ranking.isEmpty()) {
            root.addView(infoCard("Sem dados", "Marque itens com preço para criar o ranking de gastos."), matchWrapWithTop(dp(8)));
            return;
        }
        int limit = Math.min(8, ranking.size());
        double max = Math.max(1, ranking.get(0).total);
        for (int i = 0; i < limit; i++) {
            SpendingProduct product = ranking.get(i);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackground(round(cardBg(), dp(14), stroke(), 1));
            card.addView(label((i + 1) + ". " + product.name, 15, true, primaryText()));
            TextView meta = label(money.format(product.total) + " - " + formatQty(product.quantity) + " un - " + product.times + " compra(s)", 13, false, mutedText());
            meta.setPadding(0, dp(4), 0, 0);
            card.addView(meta, matchWrap());
            LinearLayout barBg = new LinearLayout(this);
            barBg.setBackground(round(inputBg(), dp(8), Color.TRANSPARENT, 0));
            LinearLayout bar = new LinearLayout(this);
            bar.setBackground(round(Color.rgb(250, 204, 21), dp(8), Color.TRANSPARENT, 0));
            int width = Math.max(dp(8), (int) (getResources().getDisplayMetrics().widthPixels * 0.72 * (product.total / max)));
            barBg.addView(bar, new LinearLayout.LayoutParams(width, dp(12)));
            card.addView(barBg, matchWrapWithTop(dp(8)));
            root.addView(card, matchWrapWithTop(dp(8)));
        }
    }

    private void addPriceInsights(Map<String, SpendingProduct> products) {
        List<SpendingProduct> insights = new ArrayList<>();
        for (SpendingProduct product : products.values()) {
            if (product.times >= 2) insights.add(product);
        }
        Collections.sort(insights, (a, b) -> Double.compare(priceSpread(b), priceSpread(a)));
        root.addView(label("Histórico de preços", 18, true, primaryText()), matchWrapWithTop(dp(16)));
        if (insights.isEmpty()) {
            root.addView(infoCard("Pouco histórico", "Quando um produto aparecer em compras diferentes, o app mostra mínimo, médio, máximo e variação."), matchWrapWithTop(dp(8)));
            return;
        }
        int limit = Math.min(6, insights.size());
        for (int i = 0; i < limit; i++) {
            SpendingProduct product = insights.get(i);
            double average = product.priceSum / product.times;
            double economy = Math.max(0, product.latestPrice - product.minPrice);
            int trendColor = economy > 0 ? Color.rgb(225, 29, 72) : Color.rgb(22, 163, 74);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackground(round(cardBg(), dp(14), stroke(), 1));
            card.addView(label(product.name, 15, true, primaryText()));

            TextView range = label("Mín. " + money.format(product.minPrice)
                    + " - Médio " + money.format(average)
                    + " - Máx. " + money.format(product.maxPrice), 13, false, mutedText());
            range.setPadding(0, dp(5), 0, 0);
            card.addView(range, matchWrap());

            TextView latest = label("Último: " + money.format(product.latestPrice)
                    + " em " + formatDateLabel(product.latestAt), 13, true, trendColor);
            latest.setPadding(0, dp(5), 0, 0);
            card.addView(latest, matchWrap());

            if (economy > 0) {
                TextView tip = label("Se comprar pelo menor histórico, economiza " + money.format(economy) + " por unidade.", 13, false, Color.rgb(22, 163, 74));
                tip.setPadding(0, dp(5), 0, 0);
                card.addView(tip, matchWrap());
            }
            root.addView(card, matchWrapWithTop(dp(8)));
        }
    }

    private double priceSpread(SpendingProduct product) {
        return product.maxPrice - product.minPrice;
    }

    private boolean sameMonth(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.MONTH) == b.get(Calendar.MONTH);
    }

    private boolean entryInSelectedRange(SpendingRecord entry) {
        if (spendingRangeMonths <= 0) return true;
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        start.add(Calendar.MONTH, -(spendingRangeMonths - 1));
        return entry.addedAt >= start.getTimeInMillis();
    }

    private String spendingRangeLabel() {
        if (spendingRangeMonths <= 0) return "Todos os registros";
        if (spendingRangeMonths == 1) return "Mês atual";
        return "Últimos " + spendingRangeMonths + " meses";
    }

    private double forecastMonthTotal(double currentTotal) {
        Calendar today = Calendar.getInstance();
        int day = Math.max(1, today.get(Calendar.DAY_OF_MONTH));
        int maxDay = today.getActualMaximum(Calendar.DAY_OF_MONTH);
        return currentTotal / day * maxDay;
    }

    private int countMonthsWithData() {
        if (spendingHistory.isEmpty()) return 6;
        Calendar first = Calendar.getInstance();
        Calendar last = Calendar.getInstance();
        long min = Long.MAX_VALUE;
        long max = 0;
        for (SpendingRecord entry : spendingHistory) {
            if (entry.price <= 0) continue;
            min = Math.min(min, entry.addedAt);
            max = Math.max(max, entry.addedAt);
        }
        if (min == Long.MAX_VALUE) return 6;
        first.setTimeInMillis(min);
        last.setTimeInMillis(max);
        int months = (last.get(Calendar.YEAR) - first.get(Calendar.YEAR)) * 12
                + last.get(Calendar.MONTH) - first.get(Calendar.MONTH) + 1;
        return Math.max(1, months);
    }

    private SpendingProduct mostBoughtProduct(Map<String, SpendingProduct> products) {
        SpendingProduct best = null;
        for (SpendingProduct product : products.values()) {
            if (best == null || product.quantity > best.quantity) best = product;
        }
        return best;
    }

    private View infoCard(String title, String body) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(round(cardBg(), dp(16), stroke(), 1));
        elevate(card, 2);
        card.addView(label(title, 18, true, primaryText()));
        TextView b = label(body, 14, false, mutedText());
        b.setPadding(0, dp(5), 0, 0);
        card.addView(b);
        return card;
    }

    private void addInputCard() {
        LinearLayout addCard = new LinearLayout(this);
        boolean compact = isCompactWidth();
        addCard.setOrientation(compact ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        addCard.setGravity(Gravity.CENTER_VERTICAL);
        addCard.setPadding(dp(12), dp(10), dp(12), dp(10));
        addCard.setBackground(round(cardBg(), dp(18), stroke(), 1));
        root.addView(addCard, matchWrapWithTop(dp(12)));

        itemInput = new AutoCompleteTextView(this);
        itemInput.setSingleLine(true);
        itemInput.setHint("Produto");
        itemInput.setTextColor(primaryText());
        itemInput.setHintTextColor(mutedText());
        itemInput.setLinkTextColor(accent());
        itemInput.setTextSize(16);
        itemInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        itemInput.setBackground(round(inputBg(), dp(14), stroke(), 1));
        itemInput.setPadding(dp(12), 0, dp(12), 0);
        addCard.addView(itemInput, compact ? matchHeight(dp(54)) : weightedHeight(dp(54)));

        priceInput = new EditText(this);
        priceInput.setSingleLine(true);
        priceInput.setHint("Preco");
        priceInput.setGravity(Gravity.CENTER_VERTICAL);
        priceInput.setTextColor(primaryText());
        priceInput.setHintTextColor(mutedText());
        priceInput.setLinkTextColor(accent());
        priceInput.setTextSize(15);
        priceInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        priceInput.setBackground(round(inputBg(), dp(14), stroke(), 1));
        priceInput.setPadding(dp(12), 0, dp(12), 0);

        unitInput = new EditText(this);
        unitInput.setSingleLine(true);
        unitInput.setHint("Un");
        unitInput.setText("1");
        unitInput.setGravity(Gravity.CENTER_VERTICAL);
        unitInput.setTextColor(primaryText());
        unitInput.setHintTextColor(mutedText());
        unitInput.setTextSize(15);
        unitInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        unitInput.setBackground(round(inputBg(), dp(14), stroke(), 1));
        unitInput.setPadding(dp(12), 0, dp(12), 0);
        setupProductSuggestions();

        Button add = button("+", Color.rgb(250, 204, 21), Color.rgb(24, 24, 27));
        add.setTextSize(22);
        add.setOnClickListener(v -> addItem());

        if (compact) {
            LinearLayout bottom = new LinearLayout(this);
            bottom.setOrientation(LinearLayout.HORIZONTAL);
            bottom.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams bottomParams = matchWrapWithTop(dp(8));
            addCard.addView(bottom, bottomParams);
            bottom.addView(priceInput, weightedHeight(dp(54)));
            LinearLayout.LayoutParams unitParams = new LinearLayout.LayoutParams(dp(70), dp(54));
            unitParams.setMargins(dp(8), 0, 0, 0);
            bottom.addView(unitInput, unitParams);
            LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(dp(56), dp(54));
            addParams.setMargins(dp(8), 0, 0, 0);
            bottom.addView(add, addParams);
        } else {
            LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(dp(110), dp(54));
            priceParams.setMargins(dp(8), 0, dp(8), 0);
            addCard.addView(priceInput, priceParams);
            LinearLayout.LayoutParams unitParams = new LinearLayout.LayoutParams(dp(74), dp(54));
            unitParams.setMargins(0, 0, dp(8), 0);
            addCard.addView(unitInput, unitParams);
            addCard.addView(add, new LinearLayout.LayoutParams(dp(56), dp(54)));
        }
    }

    private void setupProductSuggestions() {
        Map<String, ProductSuggestion> latest = productSuggestionMap();
        if (latest.isEmpty()) return;
        List<ProductSuggestion> suggestions = new ArrayList<>(latest.values());
        Collections.sort(suggestions, (a, b) -> Long.compare(b.updatedAt, a.updatedAt));
        ArrayAdapter<ProductSuggestion> adapter = new ArrayAdapter<ProductSuggestion>(this, android.R.layout.simple_dropdown_item_1line, suggestions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setBackgroundColor(cardBg());
                if (view instanceof TextView) {
                    TextView text = (TextView) view;
                    ProductSuggestion suggestion = getItem(position);
                    String price = suggestion != null && suggestion.price > 0 ? " - " + money.format(suggestion.price) : "";
                    text.setText((suggestion == null ? "" : suggestion.name) + price);
                    text.setTextColor(primaryText());
                    text.setTextSize(15);
                    text.setPadding(dp(12), dp(10), dp(12), dp(10));
                }
                return view;
            }
        };
        itemInput.setAdapter(adapter);
        itemInput.setThreshold(1);
        itemInput.setDropDownBackgroundDrawable(round(cardBg(), dp(12), stroke(), 1));
        itemInput.setOnItemClickListener((parent, view, position, id) -> {
            ProductSuggestion suggestion = (ProductSuggestion) parent.getItemAtPosition(position);
            if (suggestion == null) return;
            itemInput.setText(suggestion.name);
            itemInput.setSelection(itemInput.getText().length());
            if (suggestion.price > 0) priceInput.setText(formatPriceInput(suggestion.price));
            unitInput.setText(suggestion.unit == null || suggestion.unit.trim().isEmpty() ? "1" : suggestion.unit);
            itemInput.dismissDropDown();
        });
    }

    private Map<String, ProductSuggestion> productSuggestionMap() {
        Map<String, ProductSuggestion> latest = new LinkedHashMap<>();
        for (ShoppingList list : lists) {
            if (!list.locked && !list.archived) continue;
            long listDate = list.lockedAt > 0 ? list.lockedAt : list.createdAt;
            for (ShoppingItem item : list.items) {
                if (item.name == null || item.name.trim().isEmpty()) continue;
                long itemDate = Math.max(listDate, item.updatedAt);
                String key = normalize(item.name);
                ProductSuggestion current = latest.get(key);
                if (current == null || itemDate > current.updatedAt) {
                    latest.put(key, new ProductSuggestion(item.name, item.price, item.unit, itemDate));
                }
            }
        }
        return latest;
    }

    private void addItems() {
        ShoppingList current = lists.get(selectedIndex);
        if (current.sortMode == SORT_CHECKED_TOP) {
            addItemsByCheckedState(true);
            addItemsByCheckedState(false);
        } else if (current.sortMode == SORT_KEEP_POSITION) {
            for (int i = 0; i < current.items.size(); i++) {
                root.addView(itemRow(current.items.get(i), i), matchWrapWithTop(dp(8)));
            }
        } else {
            addItemsByCheckedState(false);
            addItemsByCheckedState(true);
        }
    }

    private void addItemsByCheckedState(boolean checked) {
        ShoppingList current = lists.get(selectedIndex);
        for (int i = 0; i < current.items.size(); i++) {
            ShoppingItem item = current.items.get(i);
            if (item.checked == checked) {
                root.addView(itemRow(item, i), matchWrapWithTop(dp(8)));
            }
        }
    }

    private View itemRow(ShoppingItem item, int index) {
        ShoppingList current = lists.get(selectedIndex);
        String qtyText = formatQty(quantityOf(item));
        String priceText = item.price > 0
                ? qtyText + " x " + money.format(item.price) + " (" + money.format(item.price * quantityOf(item)) + ")"
                : qtyText + " x R$ --";
        boolean priceBelow = true;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(9), dp(10), dp(9));
        row.setBackground(round(item.checked ? checkedBg() : cardBg(), dp(16), stroke(), 1));
        row.setOnLongClickListener(v -> {
            if (current.locked) return true;
            promptEditItem(item);
            return true;
        });

        CheckBox box = new CheckBox(this);
        box.setChecked(item.checked);
        box.setEnabled(!current.locked);
        box.setAlpha(current.locked ? 0.55f : 1.0f);
        tintCheckBox(box);
        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (current.locked) return;
            if (isChecked) {
                item.checked = true;
                if (lists.get(selectedIndex).saveCheckedToStock) {
                    addToStock(item, quantityOf(item), "un");
                }
                save();
                animateItemCheckMove(row, current, isChecked, () -> showListScreen());
            } else {
                item.checked = false;
                removeStockForItem(item);
                save();
                animateItemCheckMove(row, current, isChecked, () -> showListScreen());
            }
        });
        row.addView(box, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextSize(16);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        boolean hasHistory = hasComparablePrices(item);
        name.setTextColor(item.checked ? disabledText() :
                (hasHistory ? Color.rgb(147, 51, 234) : primaryText()));
        name.setPaintFlags(item.checked
                ? name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : name.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        if (hasHistory && !item.checked) {
            name.setPaintFlags(name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
        name.setOnClickListener(v -> showPriceComparison(item));
        name.setOnLongClickListener(v -> {
            if (current.locked) return true;
            promptEditItem(item);
            return true;
        });

        LinearLayout itemText = new LinearLayout(this);
        itemText.setOrientation(LinearLayout.VERTICAL);
        itemText.setGravity(Gravity.CENTER_VERTICAL);
        itemText.addView(name, matchWrap());

        TextView price = new TextView(this);
        price.setText(priceText);
        price.setGravity(priceBelow ? Gravity.START : Gravity.CENTER);
        price.setTextSize(14);
        price.setTypeface(Typeface.DEFAULT_BOLD);
        price.setSingleLine(false);
        price.setTextColor(item.checked ? disabledText() : accent());
        price.setOnLongClickListener(v -> {
            if (current.locked) return true;
            promptEditItem(item);
            return true;
        });
        if (priceBelow) {
            itemText.addView(price, matchWrapWithTop(dp(3)));
            row.addView(itemText, weighted());
        } else {
            row.addView(itemText, weighted());
            LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(dp(94), ViewGroup.LayoutParams.WRAP_CONTENT);
            priceParams.setMargins(dp(6), 0, dp(6), 0);
            row.addView(price, priceParams);
        }

        if (!current.locked) {
            Button remove = button("x", Color.rgb(254, 226, 226), Color.rgb(153, 27, 27));
            remove.setTextSize(18);
            remove.setOnClickListener(v -> {
                lists.get(selectedIndex).items.remove(index);
                save();
                showListScreen();
            });
            row.addView(remove, new LinearLayout.LayoutParams(dp(42), dp(42)));
        }
        return row;
    }

    private void addItem() {
        if (selectedIndex < 0 || lists.get(selectedIndex).locked) return;
        String text = itemInput.getText().toString().trim();
        if (text.isEmpty()) return;
        String unit = unitInput == null ? "" : unitInput.getText().toString().trim();
        if (unit.isEmpty()) unit = "1";
        lists.get(selectedIndex).items.add(new ShoppingItem(text, parsePrice(priceInput.getText().toString()), unit));
        save();
        hideKeyboard();
        showListScreen();
    }

    private void animateItemCheckMove(View row, ShoppingList list, boolean checked, Runnable after) {
        if (list.sortMode == SORT_KEEP_POSITION) {
            after.run();
            return;
        }
        float distance = dp(36);
        if (list.sortMode == SORT_CHECKED_TOP) {
            distance = checked ? -distance : distance;
        } else {
            distance = checked ? distance : -distance;
        }
        row.animate()
                .translationY(distance)
                .alpha(0.35f)
                .setDuration(150)
                .withEndAction(after)
                .start();
    }

    private void promptStockQuantity(ShoppingItem item, Runnable onSaved, Runnable onCancel) {
        LinearLayout form = dialogForm();
        EditText qty = dialogInput("Quantidade", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        qty.setText("1");
        EditText unit = dialogInput("Unidade", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        unit.setText(item.unit == null || item.unit.isEmpty() ? "un" : item.unit);
        form.addView(qty, matchHeight(dp(54)));
        form.addView(unit, matchWrapWithTop(dp(8)));
        dialog()
                .setTitle("Adicionar ao estoque")
                .setMessage(item.name)
                .setView(form)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    double amount = parsePrice(qty.getText().toString());
                    if (amount <= 0) amount = 1;
                    String unitText = unit.getText().toString().trim();
                    if (unitText.isEmpty()) unitText = item.unit == null || item.unit.isEmpty() ? "un" : item.unit;
                    addToStock(item, amount, unitText);
                    onSaved.run();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> onCancel.run())
                .show();
    }

    private void addToStock(ShoppingItem item, double amount, String unit) {
        long now = System.currentTimeMillis();
        if (item.stockId != null && !item.stockId.isEmpty()) {
            StockEntry existing = findStockById(item.stockId);
            if (existing != null) {
                existing.name = item.name;
                existing.quantity = amount;
                existing.unit = unit == null || unit.isEmpty() ? "un" : unit;
                existing.price = item.price;
                existing.updatedAt = now;
                if (isBlankCategory(existing.category)) existing.category = latestCategoryForProduct(item.name);
                saveStock();
                return;
            }
        }
        StockEntry linked = findStockBySourceItem(item.id);
        if (linked != null) {
            linked.name = item.name;
            linked.quantity = amount;
            linked.unit = unit == null || unit.isEmpty() ? "un" : unit;
            linked.price = item.price;
            linked.updatedAt = now;
            if (isBlankCategory(linked.category)) linked.category = latestCategoryForProduct(item.name);
            item.stockId = linked.id;
            saveStock();
            return;
        }
        StockEntry entry = new StockEntry(item.name, amount, unit, item.price, now);
        entry.sourceItemId = item.id;
        entry.category = latestCategoryForProduct(item.name);
        stock.add(0, entry);
        item.stockId = entry.id;
        saveStock();
    }

    private void removeStockForItem(ShoppingItem item) {
        StockEntry existing = findStockById(item.stockId);
        if (existing == null) existing = findStockBySourceItem(item.id);
        if (existing == null) existing = findRecentStockByItem(item);
        if (existing != null) {
            stock.remove(existing);
            saveStock();
        }
        item.stockId = "";
    }

    private void ensureSpendingRecordsForClosedLists() {
        boolean changed = false;
        for (ShoppingList list : lists) {
            if (list.locked || list.archived) {
                changed = addSpendingRecordsForList(list) || changed;
            }
        }
        if (changed) saveSpendingHistory();
    }

    private boolean addSpendingRecordsForList(ShoppingList list) {
        if (list == null || (!list.locked && !list.archived)) return false;
        boolean changed = false;
        long addedAt = list.lockedAt > 0 ? list.lockedAt : list.createdAt;
        for (ShoppingItem item : list.items) {
            if (!item.checked || item.price <= 0 || hasSpendingRecordForItem(item.id)) continue;
            SpendingRecord record = new SpendingRecord(item.name, quantityOf(item), item.unit, item.price, addedAt);
            record.sourceItemId = item.id;
            record.sourceListId = list.id;
            record.category = latestCategoryForProduct(item.name);
            StockEntry stockEntry = findStockBySourceItem(item.id);
            if (stockEntry != null && !isBlankCategory(stockEntry.category)) record.category = stockEntry.category;
            spendingHistory.add(0, record);
            changed = true;
        }
        if (changed) saveSpendingHistory();
        return changed;
    }

    private boolean hasSpendingRecordForItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) return false;
        for (SpendingRecord record : spendingHistory) {
            if (itemId.equals(record.sourceItemId)) return true;
        }
        return false;
    }

    private void updateSpendingCategoryForStock(StockEntry entry) {
        if (entry == null || isBlankCategory(entry.category)) return;
        boolean changed = false;
        for (SpendingRecord record : spendingHistory) {
            if (entry.sourceItemId.equals(record.sourceItemId)) {
                record.category = entry.category;
                changed = true;
            }
        }
        if (changed) saveSpendingHistory();
    }

    private String latestCategoryForProduct(String name) {
        String key = normalize(name);
        String category = "Outros";
        long latest = 0;
        for (SpendingRecord record : spendingHistory) {
            if (normalize(record.name).equals(key) && !isBlankCategory(record.category) && record.addedAt >= latest) {
                latest = record.addedAt;
                category = record.category;
            }
        }
        return category;
    }

    private boolean isBlankCategory(String category) {
        return category == null || category.trim().isEmpty() || "Outros".equalsIgnoreCase(category.trim());
    }

    private StockEntry findStockById(String id) {
        if (id == null || id.isEmpty()) return null;
        for (StockEntry entry : stock) {
            if (id.equals(entry.id)) return entry;
        }
        return null;
    }

    private StockEntry findStockBySourceItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        for (StockEntry entry : stock) {
            if (itemId.equals(entry.sourceItemId)) return entry;
        }
        return null;
    }

    private StockEntry findRecentStockByItem(ShoppingItem item) {
        String key = normalize(item.name);
        for (StockEntry entry : stock) {
            if (normalize(entry.name).equals(key) && Math.abs(entry.price - item.price) < 0.001) {
                return entry;
            }
        }
        return null;
    }

    private void promptEditItem(ShoppingItem item) {
        if (selectedIndex < 0 || lists.get(selectedIndex).locked) return;
        LinearLayout form = dialogForm();
        EditText name = dialogInput("Produto", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        name.setText(item.name);
        EditText price = dialogInput("Preco", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (item.price > 0) price.setText(formatPriceInput(item.price));
        EditText unit = dialogInput("Un", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        unit.setText(item.unit == null || item.unit.isEmpty() ? "1" : item.unit);
        form.addView(name, matchHeight(dp(54)));
        form.addView(price, matchWrapWithTop(dp(8)));
        form.addView(unit, matchWrapWithTop(dp(8)));
        dialog()
                .setTitle("Editar item")
                .setView(form)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String newName = name.getText().toString().trim();
                    if (!newName.isEmpty()) item.name = newName;
                    item.price = parsePrice(price.getText().toString());
                    item.unit = unit.getText().toString().trim();
                    if (item.unit.isEmpty()) item.unit = "1";
                    item.updatedAt = System.currentTimeMillis();
                    if (item.checked && selectedIndex >= 0 && lists.get(selectedIndex).saveCheckedToStock) {
                        addToStock(item, quantityOf(item), "un");
                    }
                    save();
                    showListScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void promptPrice(ShoppingItem item) {
        EditText input = new EditText(this);
        input.setHint("Preco");
        input.setSingleLine(true);
        input.setTextColor(primaryText());
        input.setHintTextColor(mutedText());
        input.setLinkTextColor(accent());
        input.setBackground(round(inputBg(), dp(12), stroke(), 1));
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (item.price > 0) {
            input.setText(formatPriceInput(item.price));
            input.setSelection(input.getText().length());
        }
        dialog()
                .setTitle("Preco de " + item.name)
                .setView(input)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    item.price = parsePrice(input.getText().toString());
                    item.updatedAt = System.currentTimeMillis();
                    save();
                    showListScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showPriceComparison(ShoppingItem item) {
        List<PriceHit> hits = findComparablePrices(item);
        if (hits.isEmpty()) {
            Toast.makeText(this, "Sem historico para este produto.", Toast.LENGTH_SHORT).show();
            return;
        }
        PriceHit hit = hits.get(0);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(16), dp(18), dp(12));
        content.setBackground(round(Color.WHITE, dp(16), Color.rgb(226, 232, 240), 1));

        TextView title = label("Comparacao de precos", 20, true, Color.rgb(15, 23, 42));
        title.setGravity(Gravity.CENTER);
        content.addView(title, matchWrap());

        addComparisonLine(content, hit.listName, 15, true);
        addComparisonLine(content, hit.itemName + ": " + money.format(hit.price), 16, false);
        addComparisonLine(content, "Data: " + formatDateLabel(hit.updatedAt), 14, false);

        double saved = item.price > 0 ? item.price - hit.price : 0;
        if (saved > 0) {
            TextView economy = label("Economia: " + money.format(saved), 16, true, Color.rgb(22, 163, 74));
            economy.setPadding(0, dp(10), 0, 0);
            content.addView(economy, matchWrap());
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(320)));

        dialog()
                .setView(scroll)
                .setPositiveButton("Fechar", null)
                .show();
    }

    private void addComparisonLine(LinearLayout content, String text, int size, boolean bold) {
        TextView line = label(text, size, bold, Color.rgb(15, 23, 42));
        line.setGravity(Gravity.START);
        line.setPadding(0, dp(10), 0, 0);
        content.addView(line, matchWrap());
    }

    private boolean hasComparablePrices(ShoppingItem item) {
        return !findComparablePrices(item).isEmpty();
    }

    private List<PriceHit> findComparablePrices(ShoppingItem item) {
        String target = normalize(item.name);
        List<PriceHit> hits = new ArrayList<>();
        for (ShoppingList list : lists) {
            for (ShoppingItem other : list.items) {
                if (other == item || other.price <= 0) continue;
                if (!normalize(other.name).equals(target)) continue;
                if (item.price > 0 && other.price >= item.price) continue;
                hits.add(new PriceHit(list.name, other.name, other.price, other.updatedAt));
            }
        }
        Collections.sort(hits, Comparator.comparingDouble(hit -> hit.price));
        return hits;
    }

    private void promptNewList() {
        LinearLayout form = dialogForm();
        EditText input = dialogInput("Nome da lista", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        form.addView(input, matchHeight(dp(54)));
        CheckBox saveToStock = new CheckBox(this);
        saveToStock.setText("Salvar itens marcados no estoque");
        saveToStock.setTextColor(primaryText());
        saveToStock.setTextSize(14);
        saveToStock.setChecked(true);
        tintCheckBox(saveToStock);
        form.addView(saveToStock, matchWrapWithTop(dp(8)));
        dialog()
                .setTitle("Nova lista")
                .setView(form)
                .setPositiveButton("Criar", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "Nova lista";
                    ShoppingList list = new ShoppingList(name);
                    list.saveCheckedToStock = saveToStock.isChecked();
                    lists.add(0, list);
                    save();
                    showHomeScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showListOptions(int index) {
        ShoppingList list = lists.get(index);
        String[] options = list.locked
                ? new String[]{"Remover"}
                : new String[]{"Editar nome", "Mudar cor", "Remover"};
        dialog()
                .setTitle(list.name)
                .setItems(options, (dialog, which) -> {
                    if (list.locked || which == 2) {
                        confirmDeleteList(index);
                    } else if (which == 0) {
                        promptEditList(index);
                    } else {
                        promptListColor(index);
                    }
                })
                .show();
    }

    private void promptEditList() {
        if (selectedIndex < 0) return;
        promptEditList(selectedIndex);
    }

    private void promptEditList(int index) {
        ShoppingList list = lists.get(index);
        if (list.locked) return;
        EditText input = dialogInput("Nome da lista", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(list.name);
        input.setSelection(input.getText().length());
        dialog()
                .setTitle("Editar lista")
                .setView(input)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) list.name = name;
                    save();
                    if (selectedIndex >= 0) showListScreen(); else showHomeScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void promptListColor(int index) {
        if (lists.get(index).locked) return;
        LinearLayout form = dialogForm();
        final int[] selected = new int[]{lists.get(index).displayColor() == 0 ? accentColor : lists.get(index).displayColor()};
        ColorSpectrumView spectrum = new ColorSpectrumView(this);
        spectrum.setSelectedColor(selected[0]);
        form.addView(spectrum, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(220)));

        View preview = new View(this);
        preview.setBackground(round(selected[0], dp(14), stroke(), 1));
        LinearLayout.LayoutParams previewParams = matchWrapWithTop(dp(10));
        previewParams.height = dp(46);
        form.addView(preview, previewParams);

        TextView values = label(colorLabel(selected[0]), 13, true, primaryText());
        values.setGravity(Gravity.CENTER);
        form.addView(values, matchWrapWithTop(dp(8)));

        TextView brightLabel = label("Brilho", 13, true, mutedText());
        form.addView(brightLabel, matchWrapWithTop(dp(10)));
        SeekBar brightness = new SeekBar(this);
        brightness.setMax(100);
        brightness.setProgress(100);
        form.addView(brightness, matchWrapWithTop(dp(4)));

        Button reset = button("Usar cor padrao", softButtonBg(), primaryText());
        form.addView(reset, matchHeight(dp(44)));

        Runnable refresh = () -> {
            preview.setBackground(round(selected[0], dp(14), stroke(), 1));
            values.setText(colorLabel(selected[0]));
        };
        spectrum.setOnColorChanged(color -> {
            selected[0] = applyBrightness(color, brightness.getProgress());
            refresh.run();
        });
        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selected[0] = applyBrightness(spectrum.baseColor(), progress);
                refresh.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        reset.setOnClickListener(v -> {
            selected[0] = 0;
            preview.setBackground(round(softButtonBg(), dp(14), stroke(), 1));
            values.setText("Padrao do app");
        });

        dialog()
                .setTitle("Cor da lista")
                .setView(form)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    lists.get(index).color = selected[0];
                    save();
                    showHomeTab();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void promptAccentColor() {
        LinearLayout form = dialogForm();
        final int defaultAccent = Color.rgb(15, 118, 110);
        final int[] selected = new int[]{accentColor};
        ColorSpectrumView spectrum = new ColorSpectrumView(this);
        spectrum.setSelectedColor(selected[0]);
        form.addView(spectrum, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(220)));

        View preview = new View(this);
        preview.setBackground(round(selected[0], dp(14), stroke(), 1));
        LinearLayout.LayoutParams previewParams = matchWrapWithTop(dp(10));
        previewParams.height = dp(46);
        form.addView(preview, previewParams);

        TextView values = label(colorLabel(selected[0]), 13, true, primaryText());
        values.setGravity(Gravity.CENTER);
        form.addView(values, matchWrapWithTop(dp(8)));

        TextView brightLabel = label("Brilho", 13, true, mutedText());
        form.addView(brightLabel, matchWrapWithTop(dp(10)));
        SeekBar brightness = new SeekBar(this);
        brightness.setMax(100);
        brightness.setProgress(100);
        form.addView(brightness, matchWrapWithTop(dp(4)));

        Button reset = button("Usar cor padrao", softButtonBg(), primaryText());
        form.addView(reset, matchHeight(dp(44)));

        Runnable refresh = () -> {
            preview.setBackground(round(selected[0], dp(14), stroke(), 1));
            values.setText(colorLabel(selected[0]));
        };
        spectrum.setOnColorChanged(color -> {
            selected[0] = applyBrightness(color, brightness.getProgress());
            refresh.run();
        });
        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selected[0] = applyBrightness(spectrum.baseColor(), progress);
                refresh.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        reset.setOnClickListener(v -> {
            selected[0] = defaultAccent;
            spectrum.setSelectedColor(defaultAccent);
            brightness.setProgress(100);
            refresh.run();
        });

        dialog()
                .setTitle("Cor do app")
                .setView(form)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    accentColor = selected[0];
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_ACCENT, accentColor).apply();
                    applySystemBars();
                    showHomeScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmDeleteList(int index) {
        dialog()
                .setTitle("Remover lista?")
                .setMessage(lists.get(index).name)
                .setPositiveButton("Remover", (dialog, which) -> {
                    lists.remove(index);
                    selectedIndex = -1;
                    save();
                    showHomeTab();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private int applyBrightness(int color, int brightness) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.max(0.08f, brightness / 100f);
        return Color.HSVToColor(hsv);
    }

    private String colorLabel(int color) {
        if (color == 0) return "Padrao do app";
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return "Hue " + Math.round(hsv[0])
                + "  Sat " + Math.round(hsv[1] * 100)
                + "  Lum " + Math.round(hsv[2] * 100)
                + "  RGB " + Color.red(color) + ", " + Color.green(color) + ", " + Color.blue(color);
    }

    private void confirmDeleteStock(StockEntry entry) {
        dialog()
                .setTitle("Remover do estoque?")
                .setMessage(entry.name)
                .setPositiveButton("Remover", (dialog, which) -> {
                    stock.remove(entry);
                    saveStock();
                    showStockWindow(false);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showStockOptions(StockEntry entry) {
        String[] options = new String[]{"Editar quantidade", "Editar categoria", "Remover"};
        dialog()
                .setTitle(entry.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        promptEditStockQuantity(entry);
                    } else if (which == 1) {
                        promptEditStockCategory(entry);
                    } else {
                        confirmDeleteStock(entry);
                    }
                })
                .show();
    }

    private void promptEditStockQuantity(StockEntry entry) {
        EditText qty = dialogInput("Un", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        qty.setText(formatQty(entry.quantity));
        qty.setSelection(qty.getText().length());
        dialog()
                .setTitle("Editar quantidade")
                .setMessage(entry.name)
                .setView(qty)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    entry.quantity = parseQuantity(qty.getText().toString());
                    entry.updatedAt = System.currentTimeMillis();
                    saveStock();
                    showStockWindow(false);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void promptEditStockCategory(StockEntry entry) {
        String[] categories = categoryOptions();
        dialog()
                .setTitle("Categoria")
                .setItems(categories, (dialog, which) -> {
                    String selected = categories[which];
                    if (CUSTOM_CATEGORY.equals(selected)) {
                        promptCustomStockCategory(entry);
                    } else {
                        setStockCategory(entry, selected);
                    }
                })
                .show();
    }

    private void promptCustomStockCategory(StockEntry entry) {
        EditText input = dialogInput("Nome da categoria", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        String current = categoryOf(entry);
        if (!isDefaultCategory(current)) {
            input.setText(current);
            input.setSelection(input.getText().length());
        }
        LinearLayout form = dialogForm();
        form.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
        ));
        dialog()
                .setTitle("Categoria personalizada")
                .setView(form)
                .setPositiveButton("Salvar", (dialog, which) -> setStockCategory(entry, input.getText().toString()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setStockCategory(StockEntry entry, String category) {
        String clean = category == null ? "" : category.trim();
        entry.category = clean.isEmpty() ? "Outros" : clean;
        entry.updatedAt = System.currentTimeMillis();
        saveStock();
        updateSpendingCategoryForStock(entry);
        showStockWindow(false);
    }

    private void shareSelectedList() {
        try {
            String link = buildShareLink(lists.get(selectedIndex));
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, link);
            startActivity(Intent.createChooser(send, "Compartilhar lista"));

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("CompraLink", link));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Nao foi possivel compartilhar esta lista.", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildShareLink(ShoppingList list) throws Exception {
        String payload = encodeCompressed(list.toJson().toString());
        return SHARE_BASE + payload;
    }

    private void handleIncomingIntent(Intent intent) {
        if (pendingIntentHandled && intent == getIntent()) return;
        if (intent == null) return;
        Uri data = intent.getData();
        if (data != null) {
            String payload = extractPayload(data);
            if (payload != null) importPayload(payload);
        }
    }

    private String extractPayload(Uri data) {
        if (data == null) return null;
        if (("http".equals(data.getScheme()) || "https".equals(data.getScheme()))
                && "compralink.app".equals(data.getHost())) {
            if (data.getPath() != null && data.getPath().startsWith("/l/")) {
                return data.getLastPathSegment();
            } else if ("/list".equals(data.getPath())) {
                return data.getQueryParameter("payload");
            }
        } else if (("http".equals(data.getScheme()) || "https".equals(data.getScheme()))
                && PAGES_HOST.equals(data.getHost())
                && data.getPath() != null
                && data.getPath().startsWith(PAGES_PATH)) {
            return data.getQueryParameter("payload");
        } else if ("compralink".equals(data.getScheme()) && "list".equals(data.getHost())) {
            return data.getQueryParameter("payload");
        }
        return null;
    }

    private String extractPayload(String text) {
        if (text == null) return null;
        String value = text.trim();
        int start = value.indexOf("http");
        if (start > 0) value = value.substring(start);
        try {
            String payload = extractPayload(Uri.parse(value));
            if (payload != null && !payload.trim().isEmpty()) return payload;
        } catch (Exception ignored) {
        }
        int marker = value.indexOf("payload=");
        if (marker >= 0) return value.substring(marker + "payload=".length());
        return null;
    }

    private void importClipboardListIfPresent() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip() || clipboard.getPrimaryClip() == null) return;
        ClipData clip = clipboard.getPrimaryClip();
        if (clip.getItemCount() == 0) return;
        CharSequence text = clip.getItemAt(0).coerceToText(this);
        String payload = extractPayload(text == null ? null : text.toString());
        if (payload == null || payload.trim().isEmpty()) return;
        String clean = cleanPayload(payload);
        String last = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_LAST_CLIPBOARD_PAYLOAD, "");
        if (clean.equals(last)) return;
        if (importPayload(clean)) {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_LAST_CLIPBOARD_PAYLOAD, clean).apply();
            Toast.makeText(this, "Lista importada da área de transferência.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean importPayload(String rawPayload) {
        if (rawPayload == null || rawPayload.trim().isEmpty()) return false;
        String payload = cleanPayload(rawPayload);
        try {
            String json = decodeCompressed(payload);
            ShoppingList imported = ShoppingList.fromJson(new JSONObject(json));
            return saveImportedList(imported);
        } catch (Exception compressedFailed) {
            try {
                byte[] decoded = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
                ShoppingList imported = ShoppingList.fromJson(new JSONObject(new String(decoded, StandardCharsets.UTF_8)));
                return saveImportedList(imported);
            } catch (Exception e) {
                Toast.makeText(this, "Link de lista inválido.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    private boolean saveImportedList(ShoppingList imported) {
        if (imported.id == null || imported.id.trim().isEmpty()) {
            imported.id = UUID.randomUUID().toString();
        }
        int existingIndex = findListIndexById(imported.id);
        if (existingIndex >= 0) {
            ShoppingList existing = lists.get(existingIndex);
            preserveLocalStockLinks(existing, imported);
            lists.set(existingIndex, imported);
            selectedIndex = existingIndex;
            save();
            Toast.makeText(this, "Lista compartilhada atualizada.", Toast.LENGTH_SHORT).show();
            return true;
        }
        clearImportedStockLinks(imported);
        lists.add(0, imported);
        selectedIndex = 0;
        save();
        Toast.makeText(this, "Lista importada.", Toast.LENGTH_SHORT).show();
        return true;
    }

    private int findListIndexById(String id) {
        if (id == null || id.isEmpty()) return -1;
        for (int i = 0; i < lists.size(); i++) {
            if (id.equals(lists.get(i).id)) return i;
        }
        return -1;
    }

    private void preserveLocalStockLinks(ShoppingList existing, ShoppingList imported) {
        for (ShoppingItem item : imported.items) {
            ShoppingItem old = findItemById(existing, item.id);
            item.stockId = old == null ? "" : old.stockId;
        }
    }

    private ShoppingItem findItemById(ShoppingList list, String id) {
        if (list == null || id == null || id.isEmpty()) return null;
        for (ShoppingItem item : list.items) {
            if (id.equals(item.id)) return item;
        }
        return null;
    }

    private void clearImportedStockLinks(ShoppingList list) {
        for (ShoppingItem item : list.items) item.stockId = "";
    }

    private String cleanPayload(String rawPayload) {
        String payload = rawPayload.trim();
        int marker = payload.indexOf("payload=");
        if (marker >= 0) payload = payload.substring(marker + "payload=".length());
        int end = payload.indexOf('\n');
        if (end >= 0) payload = payload.substring(0, end);
        int space = payload.indexOf(' ');
        if (space >= 0) payload = payload.substring(0, space);
        int amp = payload.indexOf('&');
        if (amp >= 0) payload = payload.substring(0, amp);
        return payload;
    }

    private String encodeCompressed(String json) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DeflaterOutputStream zip = new DeflaterOutputStream(out)) {
            zip.write(json.getBytes(StandardCharsets.UTF_8));
        }
        return Base64.encodeToString(out.toByteArray(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    private String decodeCompressed(String payload) throws Exception {
        byte[] packed = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InflaterInputStream unzip = new InflaterInputStream(new ByteArrayInputStream(packed))) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = unzip.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private void load() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_LISTS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            lists.clear();
            for (int i = 0; i < array.length(); i++) {
                lists.add(ShoppingList.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            lists.clear();
        }
    }

    private void loadStock() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_STOCK, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            stock.clear();
            for (int i = 0; i < array.length(); i++) {
                stock.add(StockEntry.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            stock.clear();
        }
    }

    private void loadSpendingHistory() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_SPENDING_HISTORY, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            spendingHistory.clear();
            for (int i = 0; i < array.length(); i++) {
                spendingHistory.add(SpendingRecord.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            spendingHistory.clear();
        }
    }

    private void save() {
        JSONArray array = new JSONArray();
        for (ShoppingList list : lists) {
            try {
                array.put(list.toJson());
            } catch (JSONException ignored) {
            }
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_LISTS, array.toString()).apply();
    }

    private void saveStock() {
        JSONArray array = new JSONArray();
        for (StockEntry entry : stock) {
            try {
                array.put(entry.toJson());
            } catch (JSONException ignored) {
            }
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_STOCK, array.toString()).apply();
    }

    private void saveSpendingHistory() {
        JSONArray array = new JSONArray();
        for (SpendingRecord entry : spendingHistory) {
            try {
                array.put(entry.toJson());
            } catch (JSONException ignored) {
            }
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_SPENDING_HISTORY, array.toString()).apply();
    }

    private double parsePrice(String raw) {
        if (raw == null) return 0;
        String cleaned = raw.trim().replace("R$", "").replace(" ", "");
        if (cleaned.isEmpty()) return 0;
        if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned = cleaned.replace(".", "").replace(",", ".");
        } else {
            cleaned = cleaned.replace(",", ".");
        }
        try {
            return Math.max(0, Double.parseDouble(cleaned));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatPriceInput(double value) {
        return String.format(new Locale("pt", "BR"), "%.2f", value);
    }

    private String normalize(String value) {
        String noAccent = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noAccent.trim().toLowerCase(Locale.ROOT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && itemInput != null) imm.hideSoftInputFromWindow(itemInput.getWindowToken(), 0);
    }

    private LinearLayout dialogForm() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(12), dp(12), dp(12), dp(12));
        form.setBackground(round(cardBg(), dp(14), stroke(), 1));
        return form;
    }

    private EditText dialogInput(String hint, int inputType) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setTextColor(primaryText());
        input.setHintTextColor(mutedText());
        input.setBackground(round(inputBg(), dp(12), stroke(), 1));
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setInputType(inputType);
        return input;
    }

    private StyledDialogBuilder dialog() {
        return new StyledDialogBuilder(this);
    }

    private void styleDialog(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(round(cardBg(), dp(22), stroke(), 1));
            window.setDimAmount(isDarkTheme() ? 0.72f : 0.42f);
        }
        View decor = window == null ? null : window.getDecorView();
        if (decor != null) {
            decor.setPadding(dp(2), dp(2), dp(2), dp(2));
            elevate(decor, 8);
            styleDialogTree(decor);
        }
        ListView list = dialog.getListView();
        if (list != null) {
            list.setBackgroundColor(cardBg());
            list.setDivider(null);
            list.setPadding(dp(4), dp(4), dp(4), dp(4));
        }
        styleDialogButton(dialog, AlertDialog.BUTTON_POSITIVE, accent());
        styleDialogButton(dialog, AlertDialog.BUTTON_NEGATIVE, mutedText());
        styleDialogButton(dialog, AlertDialog.BUTTON_NEUTRAL, Color.rgb(225, 29, 72));
    }

    private void styleDialogTree(View view) {
        if (view instanceof TextView && !(view instanceof Button)) {
            TextView text = (TextView) view;
            text.setTextColor(primaryText());
            text.setLinkTextColor(accent());
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                styleDialogTree(group.getChildAt(i));
            }
        }
    }

    private void styleDialogButton(AlertDialog dialog, int which, int color) {
        Button button = dialog.getButton(which);
        if (button == null) return;
        button.setTextColor(color);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private TextView label(String text, int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    private String formatQty(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private double quantityOf(ShoppingItem item) {
        if (item.unit == null || item.unit.trim().isEmpty()) return 1;
        return parseQuantity(item.unit);
    }

    private double parseQuantity(String raw) {
        if (raw == null) return 1;
        String cleaned = raw.trim();
        if (cleaned.isEmpty()) return 1;
        double value = parsePrice(cleaned);
        return value < 0 ? 0 : value;
    }

    private long stockDays(StockEntry entry) {
        long end = entry.consumedAt > 0 ? entry.consumedAt : System.currentTimeMillis();
        return Math.max(0, (end - entry.addedAt) / 86400000L);
    }

    private String formatStockAge(StockEntry entry) {
        long now = System.currentTimeMillis();
        long diff = Math.max(0, now - entry.addedAt);
        if (diff < 86400000L) {
            return formatShortDate(entry.addedAt) + " - " + formatTime(entry.addedAt);
        }
        long days = diff / 86400000L;
        return formatShortDate(entry.addedAt) + " - Há " + days + (days == 1 ? " dia" : " dias");
    }

    private String formatShortDate(long when) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(when <= 0 ? System.currentTimeMillis() : when);
        return String.format(Locale.ROOT, "%02d/%02d/%04d",
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.YEAR));
    }

    private String formatTime(long when) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(when <= 0 ? System.currentTimeMillis() : when);
        return String.format(Locale.ROOT, "%02d:%02d",
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE));
    }

    private String formatDateTime(long when) {
        return formatShortDate(when) + " às " + formatTime(when);
    }

    private String formatDateLabel(long when) {
        Calendar today = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(when <= 0 ? System.currentTimeMillis() : when);
        boolean sameDay = today.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
        if (sameDay) return "Hoje";
        return String.format(Locale.ROOT, "%02d/%02d/%04d",
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.YEAR));
    }

    private String monthKey(Calendar cal) {
        return String.format(Locale.ROOT, "%02d/%04d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
    }

    private void applySystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(accentColor);
        window.setNavigationBarColor(screenBg());
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = window.getDecorView().getSystemUiVisibility();
            if (isLightColor(accentColor)) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            if (Build.VERSION.SDK_INT >= 26) {
                if (isDarkTheme()) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                } else {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            }
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void tintCheckBox(CheckBox box) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        int unchecked = isDarkTheme() ? Color.rgb(241, 245, 249) : Color.rgb(71, 85, 105);
        int[] colors = new int[]{accent(), unchecked};
        box.setButtonTintList(new ColorStateList(states, colors));
        box.setTextColor(primaryText());
    }

    private void toggleTheme() {
        themeMode = isDarkTheme() ? THEME_LIGHT : THEME_DARK;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_THEME, themeMode).apply();
        if (selectedIndex >= 0) showListScreen(); else showHomeScreen();
    }

    private String themeIcon() {
        return isDarkTheme() ? "☀" : "☾";
    }

    private boolean isCompactWidth() {
        return getResources().getConfiguration().screenWidthDp < 390;
    }

    private boolean isDarkTheme() {
        if (themeMode == THEME_DARK) return true;
        if (themeMode == THEME_LIGHT) return false;
        int mask = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mask == Configuration.UI_MODE_NIGHT_YES;
    }

    private int screenBg() {
        return isDarkTheme() ? Color.rgb(2, 6, 23) : Color.rgb(248, 250, 252);
    }

    private int cardBg() {
        return isDarkTheme() ? Color.rgb(15, 23, 42) : Color.WHITE;
    }

    private int checkedBg() {
        return isDarkTheme() ? Color.rgb(30, 41, 59) : Color.rgb(241, 245, 249);
    }

    private int inputBg() {
        return isDarkTheme() ? Color.rgb(30, 41, 59) : Color.rgb(248, 250, 252);
    }

    private int softButtonBg() {
        return isDarkTheme() ? Color.rgb(51, 65, 85) : Color.rgb(226, 232, 240);
    }

    private int stroke() {
        return isDarkTheme() ? Color.rgb(51, 65, 85) : Color.rgb(226, 232, 240);
    }

    private int primaryText() {
        return isDarkTheme() ? Color.rgb(241, 245, 249) : Color.rgb(15, 23, 42);
    }

    private int mutedText() {
        return isDarkTheme() ? Color.rgb(148, 163, 184) : Color.rgb(71, 85, 105);
    }

    private int disabledText() {
        return isDarkTheme() ? Color.rgb(100, 116, 139) : Color.rgb(148, 163, 184);
    }

    private String categoryOf(StockEntry entry) {
        return entry.category == null || entry.category.trim().isEmpty() ? "Outros" : entry.category;
    }

    private String categoryOf(SpendingRecord entry) {
        return entry.category == null || entry.category.trim().isEmpty() ? "Outros" : entry.category;
    }

    private String[] categoryOptions() {
        return new String[]{"Mercado", "Hortifruti", "Carnes", "Limpeza", "Higiene", "Farmácia", "Bebidas", "Pet", "Outros", CUSTOM_CATEGORY};
    }

    private boolean isDefaultCategory(String category) {
        if (category == null) return true;
        String clean = category.trim();
        for (String option : categoryOptions()) {
            if (!CUSTOM_CATEGORY.equals(option) && option.equalsIgnoreCase(clean)) return true;
        }
        return false;
    }

    private int categoryColor(String category) {
        int[] colors = new int[]{
                Color.rgb(15, 118, 110),
                Color.rgb(22, 163, 74),
                Color.rgb(225, 29, 72),
                Color.rgb(37, 99, 235),
                Color.rgb(147, 51, 234),
                Color.rgb(234, 88, 12),
                Color.rgb(6, 182, 212),
                Color.rgb(202, 138, 4),
                Color.rgb(71, 85, 105)
        };
        int index = Math.abs(normalize(category).hashCode()) % colors.length;
        return colors[index];
    }

    private int accent() {
        return isDarkTheme() ? lighten(accentColor) : accentColor;
    }

    private int tintSurface(int color) {
        if (color == 0) return cardBg();
        return isDarkTheme() ? blend(color, cardBg(), 0.22f) : blend(color, cardBg(), 0.12f);
    }

    private int readableOnTint(int color) {
        if (color == 0) return primaryText();
        return isDarkTheme() ? lighten(color) : darken(color);
    }

    private int lighten(int color) {
        int r = Math.min(255, (int) (Color.red(color) * 1.25 + 24));
        int g = Math.min(255, (int) (Color.green(color) * 1.25 + 24));
        int b = Math.min(255, (int) (Color.blue(color) * 1.25 + 24));
        return Color.rgb(r, g, b);
    }

    private int darken(int color) {
        return Color.rgb(
                Math.max(0, (int) (Color.red(color) * 0.62)),
                Math.max(0, (int) (Color.green(color) * 0.62)),
                Math.max(0, (int) (Color.blue(color) * 0.62))
        );
    }

    private int blend(int color, int base, float alpha) {
        int r = (int) (Color.red(color) * alpha + Color.red(base) * (1 - alpha));
        int g = (int) (Color.green(color) * alpha + Color.green(base) * (1 - alpha));
        int b = (int) (Color.blue(color) * alpha + Color.blue(base) * (1 - alpha));
        return Color.rgb(r, g, b);
    }

    private boolean isLightColor(int color) {
        double luminance = (0.299 * Color.red(color)) + (0.587 * Color.green(color)) + (0.114 * Color.blue(color));
        return luminance > 186;
    }

    private Button button(String text, int bg, int fg) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(fg);
        button.setTextSize(14);
        button.setMinHeight(dp(48));
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackground(glowRound(bg, dp(14)));
        elevate(button, 5);
        return button;
    }

    private Button iconButton(String text, int bg, int fg) {
        Button button = button(text, bg, fg);
        button.setTextSize(22);
        button.setMinWidth(dp(48));
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    private ImageButton imageIconButton(int drawable, int bg, int fg) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(drawable);
        button.setColorFilter(fg);
        button.setBackground(glowRound(bg, dp(14)));
        button.setPadding(dp(11), dp(11), dp(11), dp(11));
        button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        elevate(button, 6);
        return button;
    }

    private int sortIcon(int sortMode) {
        if (sortMode == SORT_CHECKED_TOP) return R.drawable.ic_sort_checked_top;
        if (sortMode == SORT_KEEP_POSITION) return R.drawable.ic_sort_keep_position;
        return R.drawable.ic_sort_checked_bottom;
    }

    private GradientDrawable round(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) drawable.setStroke(strokeWidth, strokeColor);
        return drawable;
    }

    private GradientDrawable glowRound(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{lighten(color), color, darken(color)}
        );
        drawable.setCornerRadius(radius);
        drawable.setStroke(1, blend(Color.WHITE, color, isDarkTheme() ? 0.22f : 0.38f));
        return drawable;
    }

    private void elevate(View view, int amount) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.setElevation(dp(amount));
            view.setTranslationZ(dp(1));
        }
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWrapWithTop(int top) {
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, top, 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams matchHeight(int height) {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
    }

    private LinearLayout.LayoutParams weighted() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    }

    private LinearLayout.LayoutParams weightedHeight(int height) {
        return new LinearLayout.LayoutParams(0, height, 1);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class PriceHit {
        final String listName;
        final String itemName;
        final double price;
        final long updatedAt;

        PriceHit(String listName, String itemName, double price, long updatedAt) {
            this.listName = listName;
            this.itemName = itemName;
            this.price = price;
            this.updatedAt = updatedAt;
        }
    }

    private static class ProductSuggestion {
        final String name;
        final double price;
        final String unit;
        final long updatedAt;

        ProductSuggestion(String name, double price, String unit, long updatedAt) {
            this.name = name;
            this.price = price;
            this.unit = unit;
            this.updatedAt = updatedAt;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class SpendingProduct {
        final String name;
        double total;
        double quantity;
        int times;
        double priceSum;
        double minPrice;
        double maxPrice;
        double latestPrice;
        long latestAt;

        SpendingProduct(String name) {
            this.name = name;
        }
    }

    private static class ShoppingList {
        String id = UUID.randomUUID().toString();
        String name;
        int color;
        long createdAt = System.currentTimeMillis();
        long lockedAt;
        boolean saveCheckedToStock = true;
        boolean locked;
        boolean archived;
        int sortMode = SORT_CHECKED_BOTTOM;
        final List<ShoppingItem> items = new ArrayList<>();

        ShoppingList(String name) {
            this.name = name;
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("color", color);
            json.put("createdAt", createdAt);
            json.put("lockedAt", lockedAt);
            json.put("saveCheckedToStock", saveCheckedToStock);
            json.put("locked", locked);
            json.put("archived", archived);
            json.put("sortMode", sortMode);
            JSONArray array = new JSONArray();
            for (ShoppingItem item : items) array.put(item.toJson());
            json.put("items", array);
            return json;
        }

        static ShoppingList fromJson(JSONObject json) throws JSONException {
            ShoppingList list = new ShoppingList(json.optString("name", "Lista"));
            list.id = json.optString("id", UUID.randomUUID().toString());
            list.color = json.optInt("color", 0);
            list.createdAt = json.optLong("createdAt", System.currentTimeMillis());
            list.lockedAt = json.optLong("lockedAt", 0);
            list.saveCheckedToStock = json.optBoolean("saveCheckedToStock", true);
            list.locked = json.optBoolean("locked", false);
            list.archived = json.optBoolean("archived", false);
            list.sortMode = json.optInt("sortMode", SORT_CHECKED_BOTTOM);
            JSONArray array = json.optJSONArray("items");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    list.items.add(ShoppingItem.fromJson(array.getJSONObject(i)));
                }
            }
            return list;
        }

        int displayColor() {
            return color;
        }
    }

    private static class ColorSpectrumView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Bitmap bitmap;
        private int baseColor = Color.rgb(15, 118, 110);
        private OnColorChanged listener;

        ColorSpectrumView(Context context) {
            super(context);
        }

        void setSelectedColor(int color) {
            baseColor = color == 0 ? Color.rgb(15, 118, 110) : color;
            invalidate();
        }

        int baseColor() {
            return baseColor;
        }

        void setOnColorChanged(OnColorChanged listener) {
            this.listener = listener;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            bitmap = null;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (getWidth() <= 0 || getHeight() <= 0) return;
            if (bitmap == null) bitmap = buildSpectrum(getWidth(), getHeight());
            canvas.drawBitmap(bitmap, 0, 0, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            paint.setColor(Color.WHITE);
            float[] hsv = new float[3];
            Color.colorToHSV(baseColor, hsv);
            float x = hsv[0] / 360f * getWidth();
            float y = (1f - hsv[1]) * getHeight();
            canvas.drawCircle(x, y, 10, paint);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);
            canvas.drawCircle(x, y, 13, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                float x = Math.max(0, Math.min(getWidth(), event.getX()));
                float y = Math.max(0, Math.min(getHeight(), event.getY()));
                float hue = getWidth() == 0 ? 0 : (x / getWidth()) * 360f;
                float saturation = getHeight() == 0 ? 1 : 1f - (y / getHeight());
                baseColor = Color.HSVToColor(new float[]{hue, saturation, 1f});
                if (listener != null) listener.onChanged(baseColor);
                invalidate();
                return true;
            }
            return true;
        }

        private Bitmap buildSpectrum(int width, int height) {
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                float saturation = 1f - (float) y / Math.max(1, height - 1);
                for (int x = 0; x < width; x++) {
                    float hue = (float) x / Math.max(1, width - 1) * 360f;
                    pixels[y * width + x] = Color.HSVToColor(new float[]{hue, saturation, 1f});
                }
            }
            bmp.setPixels(pixels, 0, width, 0, 0, width, height);
            return bmp;
        }
    }

    private interface OnColorChanged {
        void onChanged(int color);
    }

    private class MarketGeekView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String[][] levels = buildMarketLevels();
        private char[][] board;
        private int playerX;
        private int playerY;
        private int level;
        private int moves;
        private boolean won;
        private boolean animating;
        private float animProgress = 1f;
        private int animPlayerFromX;
        private int animPlayerFromY;
        private int animPlayerToX;
        private int animPlayerToY;
        private boolean animPushing;
        private boolean animCartFull;
        private int animCartFromX;
        private int animCartFromY;
        private int animCartToX;
        private int animCartToY;
        private final Runnable animationStep = new Runnable() {
            @Override
            public void run() {
                animProgress += 0.18f;
                if (animProgress >= 1f) {
                    animProgress = 1f;
                    animating = false;
                    saveGameState();
                } else {
                    postDelayed(this, 16);
                }
                invalidate();
            }
        };

        MarketGeekView(Context context) {
            super(context);
            if (!loadSavedGame()) loadLevel(level);
        }

        void resetLevel() {
            loadLevel(level);
            saveGameState();
        }

        String status() {
            return "Fase " + (level + 1) + "/" + levels.length + " - " + moves + " movimentos";
        }

        void move(int dx, int dy) {
            if (won || animating) return;
            int nx = playerX + dx;
            int ny = playerY + dy;
            if (!inside(nx, ny) || board[ny][nx] == '#') return;
            boolean pushing = false;
            int oldPlayerX = playerX;
            int oldPlayerY = playerY;
            int oldCartX = nx;
            int oldCartY = ny;
            int newCartX = nx + dx;
            int newCartY = ny + dy;
            if (board[ny][nx] == '$' || board[ny][nx] == '*') {
                if (!inside(newCartX, newCartY) || board[newCartY][newCartX] == '#' || board[newCartY][newCartX] == '$' || board[newCartY][newCartX] == '*') return;
                pushing = true;
                board[newCartY][newCartX] = board[newCartY][newCartX] == '.' ? '*' : '$';
                board[ny][nx] = board[ny][nx] == '*' ? '.' : ' ';
            }
            playerX = nx;
            playerY = ny;
            moves++;
            startMoveAnimation(oldPlayerX, oldPlayerY, playerX, playerY, pushing, oldCartX, oldCartY, newCartX, newCartY);
            if (complete()) {
                saveBestScore(level, moves);
                if (level < levels.length - 1) {
                    level++;
                    Toast.makeText(MainActivity.this, "Fase organizada!", Toast.LENGTH_SHORT).show();
                    loadLevel(level);
                } else {
                    won = true;
                    Toast.makeText(MainActivity.this, "Mercado perfeito! Voce venceu.", Toast.LENGTH_LONG).show();
                }
            }
            saveGameState();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (board == null) return;
            canvas.drawColor(screenBg());
            int rows = board.length;
            int cols = board[0].length;
            float tile = Math.min(getWidth() / (float) cols, getHeight() / (float) rows);
            float startX = (getWidth() - tile * cols) / 2f;
            float startY = (getHeight() - tile * rows) / 2f;

            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    float left = startX + x * tile;
                    float top = startY + y * tile;
                    char tileChar = board[y][x];
                    if (animating && animPushing && x == animCartToX && y == animCartToY) {
                        tileChar = tileChar == '*' ? '.' : ' ';
                    }
                    drawTile(canvas, tileChar, left, top, tile);
                }
            }
            if (animating) {
                float px = lerp(animPlayerFromX, animPlayerToX, animProgress);
                float py = lerp(animPlayerFromY, animPlayerToY, animProgress);
                if (animPushing) {
                    float cx = lerp(animCartFromX, animCartToX, animProgress);
                    float cy = lerp(animCartFromY, animCartToY, animProgress);
                    drawCart(canvas, startX + cx * tile, startY + cy * tile, tile, animCartFull);
                }
                drawPerson(canvas, startX + px * tile, startY + py * tile, tile, animPushing, true, animProgress);
            } else {
                drawPerson(canvas, startX + playerX * tile, startY + playerY * tile, tile, false, false, 0);
            }
        }

        private void drawTile(Canvas canvas, char tileChar, float left, float top, float size) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(inputBg());
            canvas.drawRoundRect(left + 3, top + 3, left + size - 3, top + size - 3, dp(8), dp(8), paint);

            if (tileChar == '#') {
                paint.setColor(isDarkTheme() ? Color.rgb(71, 85, 105) : Color.rgb(148, 163, 184));
                canvas.drawRoundRect(left + 2, top + 2, left + size - 2, top + size - 2, dp(8), dp(8), paint);
                paint.setColor(isDarkTheme() ? Color.rgb(100, 116, 139) : Color.rgb(226, 232, 240));
                for (int i = 0; i < 3; i++) {
                    canvas.drawCircle(left + size * (0.28f + i * 0.22f), top + size * 0.35f, size * 0.08f, paint);
                    canvas.drawRect(left + size * (0.22f + i * 0.22f), top + size * 0.55f, left + size * (0.34f + i * 0.22f), top + size * 0.72f, paint);
                }
                return;
            }
            if (tileChar == '.' || tileChar == '*') {
                paint.setColor(Color.rgb(34, 197, 94));
                canvas.drawCircle(left + size / 2f, top + size / 2f, size * 0.22f, paint);
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(size * 0.24f);
                canvas.drawText("P", left + size / 2f, top + size * 0.58f, paint);
            }
            if (tileChar == '$' || tileChar == '*') {
                drawCart(canvas, left, top, size, tileChar == '*');
            }
        }

        private float lerp(float a, float b, float t) {
            return a + (b - a) * Math.min(1f, Math.max(0f, t));
        }

        private void drawCart(Canvas canvas, float left, float top, float size, boolean full) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(3, size * 0.07f));
            paint.setColor(full ? Color.rgb(22, 163, 74) : accent());
            canvas.drawLine(left + size * 0.22f, top + size * 0.28f, left + size * 0.78f, top + size * 0.28f, paint);
            canvas.drawLine(left + size * 0.3f, top + size * 0.28f, left + size * 0.42f, top + size * 0.68f, paint);
            canvas.drawLine(left + size * 0.42f, top + size * 0.68f, left + size * 0.78f, top + size * 0.68f, paint);
            if (full) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.rgb(250, 204, 21));
                canvas.drawCircle(left + size * 0.52f, top + size * 0.45f, size * 0.09f, paint);
                paint.setColor(Color.rgb(239, 68, 68));
                canvas.drawCircle(left + size * 0.66f, top + size * 0.5f, size * 0.08f, paint);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(full ? Color.rgb(22, 163, 74) : accent());
            canvas.drawCircle(left + size * 0.42f, top + size * 0.8f, size * 0.08f, paint);
            canvas.drawCircle(left + size * 0.72f, top + size * 0.8f, size * 0.08f, paint);
        }

        private void drawPerson(Canvas canvas, float left, float top, float size, boolean pushing, boolean walking, float phase) {
            float cx = left + size * 0.5f;
            float cy = top + size * 0.5f;
            float sway = walking ? (phase < 0.5f ? -1f : 1f) * size * 0.08f : 0;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(37, 99, 235));
            canvas.drawOval(cx - size * 0.16f, cy - size * 0.18f, cx + size * 0.16f, cy + size * 0.18f, paint);
            paint.setColor(Color.rgb(245, 158, 11));
            canvas.drawCircle(cx, top + size * 0.24f, size * 0.13f, paint);
            paint.setStrokeWidth(Math.max(3, size * 0.07f));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.rgb(15, 23, 42));
            if (pushing) {
                canvas.drawLine(cx - size * 0.12f, cy - size * 0.05f, cx - size * 0.28f, cy - size * 0.28f, paint);
                canvas.drawLine(cx + size * 0.12f, cy - size * 0.05f, cx + size * 0.28f, cy - size * 0.28f, paint);
            } else {
                canvas.drawLine(cx - size * 0.12f, cy, cx - size * 0.25f, cy + sway, paint);
                canvas.drawLine(cx + size * 0.12f, cy, cx + size * 0.25f, cy - sway, paint);
            }
            canvas.drawLine(cx - size * 0.08f, cy + size * 0.17f, cx - size * 0.18f, cy + size * 0.36f + sway, paint);
            canvas.drawLine(cx + size * 0.08f, cy + size * 0.17f, cx + size * 0.18f, cy + size * 0.36f - sway, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
        }

        private void startMoveAnimation(int fromX, int fromY, int toX, int toY, boolean pushing, int cartFromX, int cartFromY, int cartToX, int cartToY) {
            animPlayerFromX = fromX;
            animPlayerFromY = fromY;
            animPlayerToX = toX;
            animPlayerToY = toY;
            animPushing = pushing;
            animCartFromX = cartFromX;
            animCartFromY = cartFromY;
            animCartToX = cartToX;
            animCartToY = cartToY;
            animCartFull = pushing && inside(cartToX, cartToY) && board[cartToY][cartToX] == '*';
            animProgress = 0f;
            animating = true;
            removeCallbacks(animationStep);
            post(animationStep);
        }

        void showBestScores() {
            String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_GAME_BEST, "");
            if (raw.isEmpty()) {
                dialog()
                        .setTitle("Melhores pontuacoes")
                        .setMessage("Ainda nao ha fases concluidas.")
                        .setPositiveButton("Fechar", null)
                        .show();
                return;
            }
            StringBuilder text = new StringBuilder();
            String[] rows = raw.split(";");
            int shown = 0;
            for (String row : rows) {
                if (row.trim().isEmpty()) continue;
                String[] parts = row.split(":");
                if (parts.length != 2) continue;
                text.append("Fase ").append(Integer.parseInt(parts[0]) + 1)
                        .append(": ").append(parts[1]).append(" movimentos\n");
                shown++;
                if (shown >= 20) break;
            }
            dialog()
                    .setTitle("Melhores pontuacoes")
                    .setMessage(text.toString().trim())
                    .setPositiveButton("Fechar", null)
                    .show();
        }

        private void saveBestScore(int levelIndex, int moveCount) {
            Map<Integer, Integer> scores = bestScoreMap();
            Integer current = scores.get(levelIndex);
            if (current == null || moveCount < current) {
                scores.put(levelIndex, moveCount);
                StringBuilder raw = new StringBuilder();
                List<Integer> keys = new ArrayList<>(scores.keySet());
                Collections.sort(keys);
                for (Integer key : keys) {
                    raw.append(key).append(":").append(scores.get(key)).append(";");
                }
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_GAME_BEST, raw.toString()).apply();
            }
        }

        private Map<Integer, Integer> bestScoreMap() {
            Map<Integer, Integer> scores = new HashMap<>();
            String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_GAME_BEST, "");
            for (String row : raw.split(";")) {
                if (row.trim().isEmpty()) continue;
                String[] parts = row.split(":");
                if (parts.length != 2) continue;
                try {
                    scores.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {
                }
            }
            return scores;
        }

        private void loadLevel(int which) {
            String[] raw = levels[which];
            board = new char[raw.length][raw[0].length()];
            for (int y = 0; y < raw.length; y++) {
                for (int x = 0; x < raw[y].length(); x++) {
                    char c = raw[y].charAt(x);
                    if (c == '@') {
                        playerX = x;
                        playerY = y;
                        c = ' ';
                    }
                    board[y][x] = c;
                }
            }
            moves = 0;
            won = false;
            saveGameState();
            invalidate();
        }

        private boolean loadSavedGame() {
            String rawBoard = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_GAME_BOARD, "");
            if (rawBoard.isEmpty()) return false;
            String[] rows = rawBoard.split("\n");
            if (rows.length == 0) return false;
            level = Math.max(0, Math.min(levels.length - 1, getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_GAME_LEVEL, 0)));
            moves = Math.max(0, getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_GAME_MOVES, 0));
            playerX = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_GAME_PLAYER_X, 1);
            playerY = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_GAME_PLAYER_Y, 1);
            board = new char[rows.length][rows[0].length()];
            for (int y = 0; y < rows.length; y++) {
                if (rows[y].length() != rows[0].length()) return false;
                for (int x = 0; x < rows[y].length(); x++) board[y][x] = rows[y].charAt(x);
            }
            won = false;
            invalidate();
            return inside(playerX, playerY);
        }

        private void saveGameState() {
            if (board == null) return;
            StringBuilder raw = new StringBuilder();
            for (int y = 0; y < board.length; y++) {
                if (y > 0) raw.append('\n');
                raw.append(new String(board[y]));
            }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putInt(KEY_GAME_LEVEL, level)
                    .putInt(KEY_GAME_MOVES, moves)
                    .putInt(KEY_GAME_PLAYER_X, playerX)
                    .putInt(KEY_GAME_PLAYER_Y, playerY)
                    .putString(KEY_GAME_BOARD, raw.toString())
                    .apply();
        }

        private boolean inside(int x, int y) {
            return y >= 0 && y < board.length && x >= 0 && x < board[y].length;
        }

        private boolean complete() {
            for (char[] row : board) {
                for (char c : row) {
                    if (c == '$') return false;
                }
            }
            return true;
        }

        private String[][] buildMarketLevels() {
            String[][] base = new String[][]{
                    {"########", "# .    #", "# $##  #", "#  @   #", "#      #", "########"},
                    {"########", "#  .   #", "# ##$  #", "# @    #", "#      #", "########"},
                    {"########", "# . .  #", "# $$#  #", "#  @   #", "#      #", "########"},
                    {"########", "#   .  #", "# #$#  #", "# @ $ .#", "#      #", "########"},
                    {"#########", "# .   . #", "# $$#   #", "#  @    #", "#   #   #", "#########"},
                    {"#########", "#   .   #", "# # $ # #", "# . $ @ #", "#       #", "#########"},
                    {"#########", "# . # . #", "# $ $   #", "#   #@  #", "#       #", "#########"},
                    {"#########", "#  ..   #", "#  $$#  #", "# # @   #", "#       #", "#########"},
                    {"#########", "# .   . #", "# $ # $ #", "#   @   #", "#   #   #", "#########"},
                    {"##########", "# .    . #", "# $$ ##  #", "#  @     #", "#    #   #", "##########"},
                    {"##########", "#  . .   #", "# #$ $   #", "#   # @  #", "#        #", "##########"},
                    {"##########", "# .   #  #", "# $ $ .  #", "#   ##@  #", "#        #", "##########"},
                    {"##########", "# . . .  #", "# $$$ #  #", "#  @     #", "#    #   #", "##########"},
                    {"##########", "#   . .  #", "# # $$#  #", "# @  $ . #", "#        #", "##########"},
                    {"##########", "# . # .  #", "# $ # $  #", "#   @    #", "#        #", "##########"},
                    {"###########", "# .     . #", "# $$# #   #", "#  @  $ . #", "#    #    #", "###########"},
                    {"###########", "# . . #   #", "# $ $ # . #", "#   @ $   #", "#         #", "###########"},
                    {"###########", "#   . .   #", "# #$$$#   #", "# @       #", "#   . #   #", "###########"},
                    {"###########", "# . # . . #", "# $ # $ $ #", "#   @     #", "#     #   #", "###########"},
                    {"###########", "# .   .   #", "# $ # $ # #", "#   # @ $ #", "#       . #", "###########"},
                    {"############", "# .    .   #", "# $$#  #   #", "#  @  $  . #", "#   ##     #", "############"},
                    {"############", "# . .   .  #", "# $ $ # $  #", "#   # @    #", "#     #    #", "############"},
                    {"############", "#   . . .  #", "# # $$$ #  #", "# @        #", "#    #     #", "############"},
                    {"############", "# . # .  . #", "# $ # $$   #", "#   @   #  #", "#        # #", "############"},
                    {"############", "# .   . .  #", "# $ # $ $  #", "#   # @    #", "#     #    #", "############"}
            };
            String[][] levels = new String[50][];
            for (int i = 0; i < base.length; i++) {
                levels[i] = base[i];
                levels[i + base.length] = mirrorLevel(base[i]);
            }
            return levels;
        }

        private String[] mirrorLevel(String[] source) {
            String[] mirrored = new String[source.length];
            for (int y = 0; y < source.length; y++) {
                mirrored[y] = new StringBuilder(source[y]).reverse().toString();
            }
            return mirrored;
        }
    }

    private class StyledDialogBuilder extends AlertDialog.Builder {
        StyledDialogBuilder(Context context) {
            super(context);
        }

        @Override
        public AlertDialog.Builder setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(MainActivity.this, android.R.layout.simple_list_item_1, items) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    view.setBackgroundColor(cardBg());
                    view.setPadding(dp(10), dp(6), dp(10), dp(6));
                    if (view instanceof TextView) {
                        TextView text = (TextView) view;
                        text.setTextColor(primaryText());
                        text.setTextSize(16);
                        text.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    return getView(position, convertView, parent);
                }
            };
            return setAdapter(adapter, listener);
        }

        @Override
        public AlertDialog show() {
            AlertDialog dialog = super.show();
            styleDialog(dialog);
            return dialog;
        }
    }

    private static class ShoppingItem {
        String id = UUID.randomUUID().toString();
        String name;
        boolean checked;
        double price;
        String unit;
        long updatedAt;
        String stockId = "";

        ShoppingItem(String name, double price, String unit) {
            this.name = name;
            this.price = price;
            this.unit = unit == null || unit.isEmpty() ? "1" : unit;
            this.updatedAt = System.currentTimeMillis();
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("checked", checked);
            json.put("price", price);
            json.put("unit", unit);
            json.put("updatedAt", updatedAt);
            json.put("stockId", stockId);
            return json;
        }

        static ShoppingItem fromJson(JSONObject json) {
            ShoppingItem item = new ShoppingItem(
                    json.optString("name", "Item"),
                    json.optDouble("price", 0),
                    json.optString("unit", "1")
            );
            item.id = json.optString("id", UUID.randomUUID().toString());
            item.checked = json.optBoolean("checked", false);
            item.updatedAt = json.optLong("updatedAt", System.currentTimeMillis());
            item.stockId = json.optString("stockId", "");
            return item;
        }
    }

    private static class StockEntry {
        String id = UUID.randomUUID().toString();
        String name;
        double quantity;
        String unit;
        double price;
        long addedAt;
        long updatedAt;
        long consumedAt;
        String sourceItemId = "";
        String category = "Outros";

        StockEntry(String name, double quantity, String unit, double price, long addedAt) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit == null || unit.isEmpty() ? "un" : unit;
            this.price = price;
            this.addedAt = addedAt;
            this.updatedAt = addedAt;
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("quantity", quantity);
            json.put("unit", unit);
            json.put("price", price);
            json.put("addedAt", addedAt);
            json.put("updatedAt", updatedAt);
            json.put("consumedAt", consumedAt);
            json.put("sourceItemId", sourceItemId);
            json.put("category", category);
            return json;
        }

        static StockEntry fromJson(JSONObject json) {
            StockEntry entry = new StockEntry(
                    json.optString("name", "Item"),
                    json.optDouble("quantity", 1),
                    json.optString("unit", "un"),
                    json.optDouble("price", 0),
                    json.optLong("addedAt", System.currentTimeMillis())
            );
            entry.id = json.optString("id", UUID.randomUUID().toString());
            entry.updatedAt = json.optLong("updatedAt", entry.addedAt);
            entry.consumedAt = json.optLong("consumedAt", 0);
            entry.sourceItemId = json.optString("sourceItemId", "");
            entry.category = json.optString("category", "Outros");
            return entry;
        }
    }

    private static class SpendingRecord {
        String id = UUID.randomUUID().toString();
        String name;
        double quantity;
        String unit;
        double price;
        long addedAt;
        String sourceItemId = "";
        String sourceListId = "";
        String category = "Outros";

        SpendingRecord(String name, double quantity, String unit, double price, long addedAt) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit == null || unit.isEmpty() ? "un" : unit;
            this.price = price;
            this.addedAt = addedAt;
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("quantity", quantity);
            json.put("unit", unit);
            json.put("price", price);
            json.put("addedAt", addedAt);
            json.put("sourceItemId", sourceItemId);
            json.put("sourceListId", sourceListId);
            json.put("category", category);
            return json;
        }

        static SpendingRecord fromJson(JSONObject json) {
            SpendingRecord entry = new SpendingRecord(
                    json.optString("name", "Item"),
                    json.optDouble("quantity", 1),
                    json.optString("unit", "un"),
                    json.optDouble("price", 0),
                    json.optLong("addedAt", System.currentTimeMillis())
            );
            entry.id = json.optString("id", UUID.randomUUID().toString());
            entry.sourceItemId = json.optString("sourceItemId", "");
            entry.sourceListId = json.optString("sourceListId", "");
            entry.category = json.optString("category", "Outros");
            return entry;
        }
    }
}
