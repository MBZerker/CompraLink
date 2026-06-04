package com.codex.compralink;

import android.app.Activity;
import android.app.AlertDialog;
import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
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
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.PopupWindow;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.EnumMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

public class MainActivity extends Activity {
    private static final String PREFS = "compralink";
    private static final String KEY_LISTS = "lists";
    private static final String KEY_STOCK = "stock";
    private static final String KEY_STOCK_HISTORY = "stock_history";
    private static final String KEY_SPENDING_HISTORY = "spending_history";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_ACCENT = "accent_color";
    private static final String KEY_LAST_CLIPBOARD_PAYLOAD = "last_clipboard_payload";
    private static final String KEY_MONTHLY_GOAL = "monthly_goal";
    private static final String KEY_MONTHLY_BUDGET_LIMIT = "monthly_budget_limit";
    private static final String KEY_MONTHLY_BUDGET_INCOMES = "monthly_budget_incomes";
    private static final String KEY_MONTHLY_BUDGET_ENTRIES = "monthly_budget_entries";
    private static final String KEY_SPENDING_RANGE = "spending_range_months";
    private static final String KEY_STOCK_HISTORY_PENDING = "stock_history_pending";
    private static final String KEY_STOCK_HISTORY_SORT_DESC = "stock_history_sort_desc";
    private static final String KEY_STOCK_SORT_MODE = "stock_sort_mode";
    private static final String KEY_GAME_LEVEL = "market_game_level";
    private static final String KEY_GAME_MOVES = "market_game_moves";
    private static final String KEY_GAME_BOARD = "market_game_board";
    private static final String KEY_GAME_PLAYER_X = "market_game_player_x";
    private static final String KEY_GAME_PLAYER_Y = "market_game_player_y";
    private static final String KEY_GAME_BEST = "market_game_best";
    private static final String KEY_INVADERS_BEST = "invaders_best";
    private static final int THEME_SYSTEM = 0;
    private static final int THEME_LIGHT = 1;
    private static final int THEME_DARK = 2;
    private static final String SHARE_BASE = "https://mbzerker.github.io/CompraLink/l/?payload=";
    private static final String BACKUP_BASE = "https://mbzerker.github.io/CompraLink/l/?backup=";
    private static final String SHORTENER_ENDPOINT = "https://nbtchat-store.nectof.workers.dev/shorten";
    private static final String BACKUP_FILE_PREFIX = "CheckMercadoBackup:v2:";
    private static final String PAGES_HOST = "mbzerker.github.io";
    private static final String PAGES_PATH = "/CompraLink/l/";
    private static final String OLD_SHARE_PREFIX = "https://compralink.app/list?payload=";
    private static final String CUSTOM_SHARE_PREFIX = "compralink://list?payload=";
    private static final int SORT_CHECKED_BOTTOM = 0;
    private static final int SORT_CHECKED_TOP = 1;
    private static final int SORT_KEEP_POSITION = 2;
    private static final int STOCK_SORT_DATE = 0;
    private static final int STOCK_SORT_NAME = 1;
    private static final int STOCK_SORT_QUANTITY = 2;
    private static final int STOCK_SORT_PRICE = 3;
    private static final int STOCK_SORT_CATEGORY = 4;
    private static final int STOCK_SORT_DAYS = 5;
    private static final int REQUEST_QR_SCAN = 9021;
    private static final int REQUEST_CAMERA_PERMISSION = 9022;
    private static final int REQUEST_BACKUP_SAVE = 9023;
    private static final int REQUEST_BACKUP_OPEN = 9024;
    private static final String CUSTOM_CATEGORY = "Personalizada...";
    private static final long AUTO_LOCK_AFTER_MS = 24L * 60L * 60L * 1000L;

    private final List<ShoppingList> lists = new ArrayList<>();
    private final List<StockEntry> stock = new ArrayList<>();
    private final List<StockEntry> stockHistory = new ArrayList<>();
    private final List<SpendingRecord> spendingHistory = new ArrayList<>();
    private final List<MonthlyBudgetEntry> monthlyBudgetEntries = new ArrayList<>();
    private final List<MonthlyBudgetIncome> monthlyBudgetIncomes = new ArrayList<>();
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private LinearLayout root;
    private AutoCompleteTextView itemInput;
    private EditText priceInput;
    private EditText unitInput;
    private int selectedIndex = -1;
    private int homeTab = 0;
    private int spendingRangeMonths = 6;
    private double monthlyGoal;
    private double monthlyBudgetLimit;
    private int budgetMonthOffset;
    private boolean shellReady;
    private boolean pendingIntentHandled;
    private boolean selectedFromHistory;
    private String homeSearch = "";
    private String listSearch = "";
    private String historySearch = "";
    private String stockSearch = "";
    private String stockHistorySearch = "";
    private int searchToken;
    private String flashImportedListId = "";
    private String stockUndoStockJson;
    private String stockUndoHistoryJson;
    private String pendingBackupFileText;
    private boolean stockHistoryPending;
    private boolean stockHistorySortDesc = true;
    private int stockSortMode = STOCK_SORT_DATE;
    private final LinkedHashSet<String> selectedStockIds = new LinkedHashSet<>();
    private final Map<String, Integer> scrollPositions = new HashMap<>();
    private TextView stockSelectionStatus;
    private int homeListFilter;
    private int historyListFilter;
    private int listItemFilter;
    private String stockCategoryFilter = "";
    private String stockHistoryCategoryFilter = "";
    private int themeMode = THEME_SYSTEM;
    private int accentColor = Color.rgb(15, 118, 110);
    private int secretLogoTaps;
    private long secretLastTap;
    private int creditsSecretStep;
    private CompraInvadersView invadersView;
    private QrScannerView qrScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeMode = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_THEME, THEME_SYSTEM);
        accentColor = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_ACCENT, Color.rgb(15, 118, 110));
        monthlyGoal = Double.longBitsToDouble(getSharedPreferences(PREFS, MODE_PRIVATE).getLong(KEY_MONTHLY_GOAL, Double.doubleToLongBits(0)));
        monthlyBudgetLimit = Double.longBitsToDouble(getSharedPreferences(PREFS, MODE_PRIVATE).getLong(KEY_MONTHLY_BUDGET_LIMIT, Double.doubleToLongBits(0)));
        spendingRangeMonths = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_SPENDING_RANGE, 6);
        stockHistoryPending = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_STOCK_HISTORY_PENDING, false);
        stockHistorySortDesc = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_STOCK_HISTORY_SORT_DESC, true);
        stockSortMode = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_STOCK_SORT_MODE, STOCK_SORT_DATE);
        load();
        loadStock();
        loadStockHistory();
        loadMonthlyBudgetIncomes();
        loadSpendingHistory();
        loadMonthlyBudget();
        ensureSpendingRecordsForClosedLists();
        showSplash();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            shellReady = true;
            handleIncomingIntent(getIntent());
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
    protected void onResume() {
        super.onResume();
        UpdateManager.resumePendingInstall(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BACKUP_SAVE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                savePendingBackupToUri(data.getData());
            }
            pendingBackupFileText = null;
            return;
        }
        if (requestCode == REQUEST_BACKUP_OPEN) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                restoreBackupFromUri(data.getData());
            }
            return;
        }
        if (requestCode != REQUEST_QR_SCAN || resultCode != RESULT_OK || data == null) return;
        String result = data.getStringExtra("SCAN_RESULT");
        if (result == null) result = data.getStringExtra("com.google.zxing.client.android.SCAN_RESULT");
        if (result == null && data.getData() != null) result = data.getData().toString();
        if (result == null || result.trim().isEmpty()) {
            Toast.makeText(this, "Nao foi possivel ler o QR Code.", Toast.LENGTH_SHORT).show();
            return;
        }
        importFiscalNoteFromUrl(result.trim());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showFiscalQrScanner();
        }
    }

    @Override
    public void onBackPressed() {
        if (homeTab == 5 && selectedIndex >= 0) {
            showListScreen();
            return;
        }
        if (homeTab == 4) {
            showHomeScreen();
            return;
        }
        if (homeTab == 7) {
            if (invadersView != null) invadersView.stop();
            invadersView = null;
            showHomeScreen();
            return;
        }
        if (homeTab == 8) {
            stopQrScanner();
            showHomeScreen();
            return;
        }
        if (selectedIndex >= 0 || homeTab != 0) {
            selectedIndex = -1;
            if (selectedFromHistory) {
                selectedFromHistory = false;
                showHistoryScreen();
            } else {
                homeTab = 0;
                showHomeScreen();
            }
            return;
        }
        super.onBackPressed();
    }

    private void showSplash() {
        applySystemBars();
        FrameLayout splash = new FrameLayout(this);
        splash.setBackgroundColor(Color.WHITE);

        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.splash_art);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        splash.addView(image, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        setContentView(splash);
    }

    private void showHomeScreen() {
        selectedIndex = -1;
        selectedFromHistory = false;
        homeTab = selectedFromHistory ? 3 : 0;
        updateAutoLockedLists();
        buildRoot();
        addTopHeader("Suas listas", "Crie listas e compare precos salvos.", false);
        addSearchBar("Pesquisar listas ou itens", homeSearch, value -> {
            homeSearch = value;
            showHomeScreen();
        });

        boolean shown = false;
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).archived || lists.get(i).deletedFromHistory) continue;
            if (!matchesHomeListFilter(lists.get(i))) continue;
            if (!matchesListSearch(lists.get(i), homeSearch)) continue;
            shown = true;
            root.addView(listCard(i), matchWrapWithTop(dp(10)));
        }
        if (!shown) {
            root.addView(homeSearch == null || homeSearch.trim().isEmpty()
                    ? infoCard("Nenhuma lista criada", "Toque no carrinho para criar sua primeira lista.")
                    : infoCard("Nada encontrado", "Nenhuma lista ou item corresponde a pesquisa."), matchWrapWithTop(dp(10)));
        }
        setContentView(rootScroll());
    }

    private void showHistoryScreen() {
        selectedIndex = -1;
        selectedFromHistory = false;
        homeTab = 3;
        updateAutoLockedLists();
        buildRoot();
        addTopHeader("Historico", "Listas protegidas ficam guardadas aqui.", false);
        addHistorySummary();
        addSearchBar("Pesquisar historico ou itens", historySearch, value -> {
            historySearch = value;
            showHistoryScreen();
        });

        boolean shown = false;
        for (int i = 0; i < lists.size(); i++) {
            if (!lists.get(i).archived || lists.get(i).deletedFromHistory) continue;
            if (!matchesHistoryListFilter(lists.get(i))) continue;
            if (!matchesListSearch(lists.get(i), historySearch)) continue;
            shown = true;
            root.addView(listCard(i), matchWrapWithTop(dp(10)));
        }
        if (!shown) {
            root.addView(historySearch == null || historySearch.trim().isEmpty()
                    ? infoCard("Historico vazio", "Listas completas aparecem aqui depois de protegidas automaticamente.")
                    : infoCard("Nada encontrado", "Nenhuma lista do historico corresponde a pesquisa."), matchWrapWithTop(dp(10)));
        }
        setContentView(rootScroll());
    }

    private void addHistorySummary() {
        int count = 0;
        int items = 0;
        double total = 0;
        for (ShoppingList list : lists) {
            if (!list.archived || list.deletedFromHistory) continue;
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
        selectedFromHistory = false;
        homeTab = spending ? 2 : 1;
        buildRoot();
        addTopHeader(spending ? "Gastos" : "Estoque",
                spending ? "Resumo e graficos das compras registradas." : "Itens comprados e duracao estimada.",
                false);
        addStockTabs();
        if (spending) {
            addSpendingScreen();
        } else {
            addSearchBar("Pesquisar itens do estoque", stockSearch, value -> {
                stockSearch = value;
                showStockWindow(false);
            });
            addStockScreen();
        }
        setContentView(rootScroll());
    }

    private void showMonthlyBudgetScreen() {
        selectedIndex = -1;
        selectedFromHistory = false;
        homeTab = 9;
        buildRoot();
        String monthKey = selectedBudgetMonthKey();
        addTopHeader("Or\u00e7amento mensal", "Planejamento de " + budgetMonthTitle(monthKey) + ".", false);
        addMonthlyBudgetSummary();
        addMonthlyBudgetActions();
        addMonthlyBudgetIncomeList();
        addMonthlyBudgetEntries();
        setContentView(rootScroll());
    }

    private void addMonthlyBudgetSummary() {
        String monthKey = selectedBudgetMonthKey();
        double planned = monthlyBudgetPlanned(monthKey);
        double paid = monthlyBudgetPaid(monthKey);
        double pending = Math.max(0, planned - paid);
        double income = monthlyBudgetIncomeTotal(monthKey);
        double ceiling = monthlyBudgetLimit + income;
        double available = ceiling <= 0 ? -planned : ceiling - planned;
        int statusColor = ceiling <= 0
                ? accent()
                : (available >= 0 ? Color.rgb(22, 163, 74) : Color.rgb(225, 29, 72));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(glassCardBg(statusColor));
        elevate(card, 5);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top, matchWrap());

        FrameLayout iconFrame = new FrameLayout(this);
        iconFrame.setBackground(softPillBg(statusColor));
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_budget_chart);
        icon.setColorFilter(statusColor);
        iconFrame.addView(icon, new FrameLayout.LayoutParams(dp(26), dp(26), Gravity.CENTER));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(52), dp(52));
        iconParams.setMargins(0, 0, dp(12), 0);
        top.addView(iconFrame, iconParams);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        top.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        texts.addView(label("Resumo de " + budgetMonthTitle(monthKey), 18, true, primaryText()));
        String limitText = ceiling <= 0
                ? "Defina um teto ou registre entradas para acompanhar o m\u00eas."
                : "Teto " + money.format(monthlyBudgetLimit) + " + entradas " + money.format(income);
        TextView subtitle = label(limitText, 13, false, mutedText());
        subtitle.setPadding(0, dp(4), 0, 0);
        texts.addView(subtitle);

        TextView status = label(ceiling <= 0 ? "Sem limite" : (available >= 0 ? "Dentro" : "Acima"), 13, true, statusColor);
        status.setPadding(dp(10), dp(4), dp(10), dp(4));
        status.setBackground(softPillBg(statusColor));
        top.addView(status);

        if (ceiling > 0) {
            card.addView(progressBarView(planned / ceiling, statusColor), matchHeightWithTop(dp(10), dp(12)));
        }
        root.addView(card, matchWrapWithTop(dp(10)));

        root.addView(metricCard("Previsto", money.format(planned), primaryText(), R.drawable.ic_report, Color.rgb(57, 229, 108)), matchWrapWithTop(dp(8)));
        root.addView(metricCard("Pago", money.format(paid), Color.rgb(22, 163, 74), R.drawable.ic_money_circle, Color.rgb(22, 163, 74)), matchWrapWithTop(dp(8)));
        root.addView(metricCard("Pendente", money.format(pending), pending > 0 ? Color.rgb(234, 179, 8) : Color.rgb(22, 163, 74), R.drawable.ic_calendar_tiny, Color.rgb(234, 179, 8)), matchWrapWithTop(dp(8)));
        root.addView(metricCard("Saldo", ceiling <= 0 ? "Sem limite" : money.format(available), statusColor, R.drawable.ic_target, statusColor), matchWrapWithTop(dp(8)));
    }

    private void addMonthlyBudgetActions() {
        LinearLayout monthTools = iconStrip();
        addWeightedStripIcon(monthTools, R.drawable.ic_back, isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText(), true, v -> {
            budgetMonthOffset--;
            showMonthlyBudgetScreen();
        });
        addWeightedStripIcon(monthTools, R.drawable.ic_calendar_tiny, isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent(), true, v -> {
            budgetMonthOffset = 0;
            showMonthlyBudgetScreen();
        });
        addWeightedStripIcon(monthTools, R.drawable.ic_arrow_right, isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText(), true, v -> {
            budgetMonthOffset++;
            showMonthlyBudgetScreen();
        });
        root.addView(monthTools, matchHeightWithTop(dp(50), dp(10)));

        LinearLayout tools = iconStrip();
        addWeightedStripIcon(tools, R.drawable.ic_target, isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent(), true, v -> promptMonthlyBudgetLimit());
        addWeightedStripIcon(tools, R.drawable.ic_money_circle, Color.rgb(22, 163, 74), true, v -> promptMonthlyBudgetIncome(null));
        addWeightedStripIcon(tools, R.drawable.ic_plus, isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent(), true, v -> promptMonthlyBudgetEntry(null));
        root.addView(tools, matchHeightWithTop(dp(54), dp(10)));
    }

    private void addMonthlyBudgetIncomeList() {
        List<MonthlyBudgetIncome> rows = budgetIncomesForSelectedMonth();
        if (rows.isEmpty()) {
            root.addView(infoCard("Entradas", "Use o bot\u00e3o de dinheiro para registrar sal\u00e1rio, pagamentos recebidos, vendas ou qualquer entrada do m\u00eas."), matchWrapWithTop(dp(10)));
            return;
        }
        root.addView(label("Entradas do m\u00eas", 18, true, primaryText()), matchWrapWithTop(dp(16)));
        for (MonthlyBudgetIncome income : rows) {
            root.addView(monthlyBudgetIncomeCard(income), matchWrapWithTop(dp(10)));
        }
    }

    private View monthlyBudgetIncomeCard(MonthlyBudgetIncome income) {
        int color = Color.rgb(22, 163, 74);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(12), dp(12), dp(12));
        card.setBackground(glassCardBg(color));
        elevate(card, 3);
        card.setOnClickListener(v -> showMonthlyBudgetIncomeOptions(income));
        card.setOnLongClickListener(v -> {
            showMonthlyBudgetIncomeOptions(income);
            return true;
        });

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        card.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        texts.addView(label(isBlank(income.description) ? "Entrada" : income.description, 16, true, primaryText()));
        TextView value = label(money.format(income.amount), 15, true, color);
        value.setPadding(0, dp(4), 0, 0);
        texts.addView(value);

        ImageButton more = moreMenuButton(mutedText());
        more.setOnClickListener(v -> showMonthlyBudgetIncomeOptions(income));
        card.addView(more, new LinearLayout.LayoutParams(dp(46), dp(42)));
        return card;
    }

    private void showMonthlyBudgetIncomeOptions(MonthlyBudgetIncome income) {
        dialog()
                .setTitle(isBlank(income.description) ? "Entrada" : income.description)
                .setItems(new String[]{"Editar", "Remover"}, (dialog, which) -> {
                    if (which == 0) {
                        promptMonthlyBudgetIncome(income);
                    } else {
                        confirmRemoveMonthlyBudgetIncome(income);
                    }
                })
                .show();
    }

    private void confirmRemoveMonthlyBudgetIncome(MonthlyBudgetIncome income) {
        dialog()
                .setTitle("Remover entrada?")
                .setMessage(isBlank(income.description) ? "Entrada" : income.description)
                .setPositiveButton("Remover", (dialog, which) -> {
                    monthlyBudgetIncomes.remove(income);
                    saveMonthlyBudgetIncomes();
                    showMonthlyBudgetScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void promptMonthlyBudgetIncome(MonthlyBudgetIncome income) {
        boolean editing = income != null;
        MonthlyBudgetIncome target = editing ? income : new MonthlyBudgetIncome();
        LinearLayout form = dialogForm();

        EditText description = dialogInput("Descri\u00e7\u00e3o da entrada", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        description.setText(target.description);
        configureSelectAll(description);
        form.addView(description, matchHeightWithTop(dp(54), 0));

        EditText amount = dialogInput("Valor recebido", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setDecimalInput(amount);
        configureSelectAll(amount);
        if (target.amount > 0) amount.setText(formatPriceInput(target.amount));
        form.addView(amount, matchHeightWithTop(dp(54), dp(8)));

        dialog()
                .setTitle(editing ? "Editar entrada" : "Nova entrada")
                .setMessage("Esta entrada soma ao teto de " + budgetMonthTitle(selectedBudgetMonthKey()) + ".")
                .setView(form)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    target.description = description.getText().toString().trim();
                    if (isBlank(target.description)) target.description = "Entrada";
                    target.amount = parsePrice(amount.getText().toString());
                    if (isBlank(target.monthKey)) target.monthKey = selectedBudgetMonthKey();
                    target.updatedAt = System.currentTimeMillis();
                    if (!editing) monthlyBudgetIncomes.add(0, target);
                    saveMonthlyBudgetIncomes();
                    showMonthlyBudgetScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void addMonthlyBudgetEntries() {
        List<MonthlyBudgetEntry> rows = budgetEntriesForSelectedMonth();
        if (rows.isEmpty()) {
            root.addView(infoCard("Sem despesas em " + budgetMonthTitle(selectedBudgetMonthKey()), "Use + para registrar contas, parcelas, vencimentos e categorias deste m\u00eas."), matchWrapWithTop(dp(10)));
            return;
        }
        Collections.sort(rows, (a, b) -> {
            if (a.paid != b.paid) return a.paid ? 1 : -1;
            int due = Integer.compare(normalizedDueDay(a.dueDay), normalizedDueDay(b.dueDay));
            if (due != 0) return due;
            return normalize(a.description).compareTo(normalize(b.description));
        });
        root.addView(label("Despesas de " + budgetMonthTitle(selectedBudgetMonthKey()), 18, true, primaryText()), matchWrapWithTop(dp(16)));
        for (MonthlyBudgetEntry entry : rows) {
            root.addView(monthlyBudgetEntryCard(entry), matchWrapWithTop(dp(10)));
        }
    }

    private View monthlyBudgetEntryCard(MonthlyBudgetEntry entry) {
        int color = budgetCategoryColor(entry.category);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(12), dp(14));
        card.setBackground(glassCardBg(color));
        elevate(card, 4);
        card.setOnClickListener(v -> showMonthlyBudgetEntryOptions(entry));
        card.setOnLongClickListener(v -> {
            showMonthlyBudgetEntryOptions(entry);
            return true;
        });

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top, matchWrap());

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        top.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView title = label(isBlank(entry.description) ? "Despesa" : entry.description, 18, true, primaryText());
        texts.addView(title);
        TextView meta = label("Vencimento " + budgetDueLabel(entry.dueDay, entry.monthKey) + " \u00b7 " + dueStatus(entry.dueDay, entry.monthKey), 13, false, mutedText());
        meta.setPadding(0, dp(4), 0, 0);
        texts.addView(meta);

        TextView amount = label(money.format(entry.amount), 14, true, entry.paid ? Color.rgb(22, 163, 74) : color);
        amount.setPadding(dp(10), dp(4), dp(10), dp(4));
        amount.setBackground(softPillBg(entry.paid ? Color.rgb(22, 163, 74) : color));
        top.addView(amount);

        ImageButton more = moreMenuButton(mutedText());
        more.setOnClickListener(v -> showMonthlyBudgetEntryOptions(entry));
        top.addView(more, new LinearLayout.LayoutParams(dp(46), dp(42)));

        LinearLayout detail = new LinearLayout(this);
        detail.setOrientation(LinearLayout.VERTICAL);
        detail.setPadding(0, dp(8), 0, 0);
        card.addView(detail);

        detail.addView(iconText(R.drawable.ic_calendar_tiny, "Dias para vencer: " + daysUntilDueLabel(entry.dueDay, entry.monthKey), 13, false, mutedText(), mutedText()));
        if (!isBlank(entry.installment)) {
            TextView parcel = label("Qtde/parcela: " + entry.installment, 13, false, mutedText());
            parcel.setPadding(0, dp(4), 0, 0);
            detail.addView(parcel);
        }
        if (entry.paid) {
            String paidAt = isBlank(entry.paidAt) ? "Hoje" : entry.paidAt;
            TextView paid = label("Pago em: " + paidAt, 13, true, Color.rgb(22, 163, 74));
            paid.setPadding(0, dp(4), 0, 0);
            detail.addView(paid);
        }
        if (!isBlank(entry.paymentMethod)) {
            TextView payment = label("Forma de pagamento: " + entry.paymentMethod, 13, false, mutedText());
            payment.setPadding(0, dp(4), 0, 0);
            detail.addView(payment);
        }
        LinearLayout category = iconText(R.drawable.ic_tag_tiny, "Categoria: " + (isBlank(entry.category) ? "Sem categoria" : entry.category), 13, false, mutedText(), color);
        category.setPadding(0, dp(4), 0, 0);
        detail.addView(category);
        TextView percent = label("Participa\u00e7\u00e3o no m\u00eas: " + monthlyBudgetPercent(entry), 13, true, color);
        percent.setPadding(0, dp(4), 0, 0);
        detail.addView(percent);
        return card;
    }

    private void showMonthlyBudgetEntryOptions(MonthlyBudgetEntry entry) {
        String paidOption = entry.paid ? "Marcar como pendente" : "Marcar como pago";
        String copyOption = "Copiar para " + budgetMonthTitle(nextMonthKey(entry.monthKey));
        dialog()
                .setTitle(isBlank(entry.description) ? "Despesa" : entry.description)
                .setItems(new String[]{paidOption, copyOption, "Editar", "Remover"}, (dialog, which) -> {
                    if (which == 0) {
                        entry.paid = !entry.paid;
                        entry.paidAt = entry.paid && isBlank(entry.paidAt) ? formatDayMonth(System.currentTimeMillis()) : entry.paidAt;
                        entry.updatedAt = System.currentTimeMillis();
                        saveMonthlyBudget();
                        showMonthlyBudgetScreen();
                    } else if (which == 1) {
                        copyMonthlyBudgetEntryToNextMonth(entry);
                    } else if (which == 2) {
                        promptMonthlyBudgetEntry(entry);
                    } else {
                        confirmRemoveMonthlyBudgetEntry(entry);
                    }
                })
                .show();
    }

    private void copyMonthlyBudgetEntryToNextMonth(MonthlyBudgetEntry source) {
        MonthlyBudgetEntry copy = new MonthlyBudgetEntry();
        copy.description = source.description;
        copy.installment = source.installment;
        copy.amount = source.amount;
        copy.dueDay = source.dueDay;
        copy.paymentMethod = source.paymentMethod;
        copy.category = source.category;
        copy.monthKey = nextMonthKey(source.monthKey);
        monthlyBudgetEntries.add(0, copy);
        saveMonthlyBudget();
        budgetMonthOffset = monthOffsetFromNow(copy.monthKey);
        showMonthlyBudgetScreen();
    }

    private void confirmRemoveMonthlyBudgetEntry(MonthlyBudgetEntry entry) {
        dialog()
                .setTitle("Remover despesa?")
                .setMessage(isBlank(entry.description) ? "Despesa" : entry.description)
                .setPositiveButton("Remover", (dialog, which) -> {
                    monthlyBudgetEntries.remove(entry);
                    saveMonthlyBudget();
                    showMonthlyBudgetScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void promptMonthlyBudgetLimit() {
        EditText limit = dialogInput("Teto mensal", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setDecimalInput(limit);
        configureSelectAll(limit);
        if (monthlyBudgetLimit > 0) limit.setText(formatPriceInput(monthlyBudgetLimit));
        dialog()
                .setTitle("Teto mensal")
                .setMessage("Defina o teto base de gastos. Entradas registradas no m\u00eas somam a esse teto.")
                .setView(limit)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    monthlyBudgetLimit = parsePrice(limit.getText().toString());
                    saveMonthlyBudget();
                    showMonthlyBudgetScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void promptMonthlyBudgetEntry(MonthlyBudgetEntry entry) {
        boolean editing = entry != null;
        MonthlyBudgetEntry target = editing ? entry : new MonthlyBudgetEntry();
        LinearLayout form = dialogForm();

        EditText description = dialogInput("Descri\u00e7\u00e3o", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        description.setText(target.description);
        configureSelectAll(description);
        form.addView(description, matchHeightWithTop(dp(54), 0));

        EditText amount = dialogInput("Valor", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setDecimalInput(amount);
        configureSelectAll(amount);
        if (target.amount > 0) amount.setText(formatPriceInput(target.amount));
        form.addView(amount, matchHeightWithTop(dp(54), dp(8)));

        EditText dueDay = dialogInput("Dia do vencimento (1 a 31)", InputType.TYPE_CLASS_NUMBER);
        dueDay.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        if (editing && target.dueDay > 0) dueDay.setText(String.valueOf(target.dueDay));
        configureSelectAll(dueDay);
        form.addView(dueDay, matchHeightWithTop(dp(54), dp(8)));

        EditText installment = dialogInput("Qtde/parcela (ex.: 6/12)", InputType.TYPE_CLASS_TEXT);
        installment.setText(target.installment);
        form.addView(installment, matchHeightWithTop(dp(54), dp(8)));

        EditText payment = dialogInput("Forma de pagamento", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        payment.setText(target.paymentMethod);
        form.addView(payment, matchHeightWithTop(dp(54), dp(8)));

        EditText category = dialogInput("Categoria", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        category.setText(target.category);
        form.addView(category, matchHeightWithTop(dp(54), dp(8)));

        CheckBox paid = new CheckBox(this);
        paid.setText("Despesa paga");
        paid.setChecked(target.paid);
        tintCheckBox(paid);
        form.addView(paid, matchWrapWithTop(dp(8)));

        EditText paidAt = dialogInput("Pago em (DD/MM)", InputType.TYPE_CLASS_TEXT);
        paidAt.setText(target.paidAt);
        form.addView(paidAt, matchHeightWithTop(dp(54), dp(8)));

        dialog()
                .setTitle(editing ? "Editar despesa" : "Nova despesa mensal")
                .setMessage("Vencimento \u00e9 o dia do m\u00eas. Ex.: 5 = dia 05 de " + budgetMonthTitle(selectedBudgetMonthKey()) + ".")
                .setView(form)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    target.description = description.getText().toString().trim();
                    if (isBlank(target.description)) target.description = "Despesa";
                    target.amount = parsePrice(amount.getText().toString());
                    target.dueDay = parseDueDay(dueDay.getText().toString());
                    target.installment = installment.getText().toString().trim();
                    target.paymentMethod = payment.getText().toString().trim();
                    target.category = category.getText().toString().trim();
                    target.paid = paid.isChecked();
                    target.paidAt = target.paid
                            ? (isBlank(paidAt.getText().toString()) ? formatDayMonth(System.currentTimeMillis()) : paidAt.getText().toString().trim())
                            : "";
                    if (isBlank(target.monthKey)) target.monthKey = selectedBudgetMonthKey();
                    target.updatedAt = System.currentTimeMillis();
                    if (!editing) monthlyBudgetEntries.add(0, target);
                    saveMonthlyBudget();
                    showMonthlyBudgetScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private List<MonthlyBudgetEntry> budgetEntriesForSelectedMonth() {
        String monthKey = selectedBudgetMonthKey();
        List<MonthlyBudgetEntry> rows = new ArrayList<>();
        for (MonthlyBudgetEntry entry : monthlyBudgetEntries) {
            if (isBlank(entry.monthKey)) entry.monthKey = currentBudgetMonthKey();
            if (monthKey.equals(entry.monthKey)) rows.add(entry);
        }
        return rows;
    }

    private List<MonthlyBudgetIncome> budgetIncomesForSelectedMonth() {
        String monthKey = selectedBudgetMonthKey();
        List<MonthlyBudgetIncome> rows = new ArrayList<>();
        for (MonthlyBudgetIncome income : monthlyBudgetIncomes) {
            if (isBlank(income.monthKey)) income.monthKey = currentBudgetMonthKey();
            if (monthKey.equals(income.monthKey)) rows.add(income);
        }
        return rows;
    }

    private double monthlyBudgetPlanned(String monthKey) {
        double total = 0;
        for (MonthlyBudgetEntry entry : monthlyBudgetEntries) {
            if (isBlank(entry.monthKey)) entry.monthKey = currentBudgetMonthKey();
            if (monthKey.equals(entry.monthKey)) total += entry.amount;
        }
        return total;
    }

    private double monthlyBudgetPaid(String monthKey) {
        double total = 0;
        for (MonthlyBudgetEntry entry : monthlyBudgetEntries) {
            if (isBlank(entry.monthKey)) entry.monthKey = currentBudgetMonthKey();
            if (monthKey.equals(entry.monthKey) && entry.paid) total += entry.amount;
        }
        return total;
    }

    private double monthlyBudgetIncomeTotal(String monthKey) {
        double total = 0;
        for (MonthlyBudgetIncome income : monthlyBudgetIncomes) {
            if (isBlank(income.monthKey)) income.monthKey = currentBudgetMonthKey();
            if (monthKey.equals(income.monthKey)) total += income.amount;
        }
        return total;
    }

    private int budgetCategoryColor(String category) {
        if (isBlank(category)) return accent();
        return categoryColor(category);
    }

    private String monthlyBudgetPercent(MonthlyBudgetEntry entry) {
        double total = monthlyBudgetPlanned(isBlank(entry.monthKey) ? selectedBudgetMonthKey() : entry.monthKey);
        if (total <= 0 || entry == null) return "0%";
        return String.format(Locale.ROOT, "%.0f%%", (entry.amount / total) * 100.0);
    }

    private int parseDueDay(String raw) {
        try {
            int value = Integer.parseInt(raw == null ? "" : raw.trim());
            return Math.max(1, Math.min(31, value));
        } catch (NumberFormatException e) {
            Calendar now = Calendar.getInstance();
            return now.get(Calendar.DAY_OF_MONTH);
        }
    }

    private int normalizedDueDay(int day) {
        return Math.max(1, Math.min(31, day <= 0 ? 1 : day));
    }

    private String selectedBudgetMonthKey() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, budgetMonthOffset);
        return monthKey(cal);
    }

    private String currentBudgetMonthKey() {
        return monthKey(Calendar.getInstance());
    }

    private Calendar calendarFromMonthKey(String key) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        try {
            String[] parts = (key == null ? "" : key).split("/");
            if (parts.length == 2) {
                cal.set(Calendar.MONTH, Math.max(0, Math.min(11, Integer.parseInt(parts[0]) - 1)));
                cal.set(Calendar.YEAR, Integer.parseInt(parts[1]));
            }
        } catch (Exception ignored) {
        }
        return cal;
    }

    private String budgetMonthTitle(String key) {
        Calendar cal = calendarFromMonthKey(key);
        String[] months = {"Janeiro", "Fevereiro", "Mar\u00e7o", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        return months[cal.get(Calendar.MONTH)] + "/" + cal.get(Calendar.YEAR);
    }

    private String budgetDueLabel(int dueDay, String monthKey) {
        Calendar cal = calendarFromMonthKey(isBlank(monthKey) ? selectedBudgetMonthKey() : monthKey);
        int day = Math.min(normalizedDueDay(dueDay), cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return String.format(Locale.ROOT, "%02d/%02d/%04d", day, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
    }

    private String nextMonthKey(String monthKey) {
        Calendar cal = calendarFromMonthKey(isBlank(monthKey) ? selectedBudgetMonthKey() : monthKey);
        cal.add(Calendar.MONTH, 1);
        return monthKey(cal);
    }

    private int monthOffsetFromNow(String key) {
        Calendar now = Calendar.getInstance();
        Calendar target = calendarFromMonthKey(key);
        return (target.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 + target.get(Calendar.MONTH) - now.get(Calendar.MONTH);
    }

    private String formatDayMonth(long when) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(when <= 0 ? System.currentTimeMillis() : when);
        return String.format(Locale.ROOT, "%02d/%02d", date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH) + 1);
    }

    private long daysUntilDue(int dueDay, String monthKey) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Calendar due = calendarFromMonthKey(isBlank(monthKey) ? selectedBudgetMonthKey() : monthKey);
        int day = Math.min(normalizedDueDay(dueDay), due.getActualMaximum(Calendar.DAY_OF_MONTH));
        due.set(Calendar.DAY_OF_MONTH, day);
        return (due.getTimeInMillis() - today.getTimeInMillis()) / 86400000L;
    }

    private String daysUntilDueLabel(int dueDay, String monthKey) {
        long days = daysUntilDue(dueDay, monthKey);
        if (days == 0) return "hoje";
        if (days > 0) return days + (days == 1 ? " dia" : " dias");
        long late = Math.abs(days);
        return "atrasado h\u00e1 " + late + (late == 1 ? " dia" : " dias");
    }

    private String dueStatus(int dueDay, String monthKey) {
        long days = daysUntilDue(dueDay, monthKey);
        if (days == 0) return "vence hoje";
        if (days > 0) return "faltam " + days + (days == 1 ? " dia" : " dias");
        long late = Math.abs(days);
        return "atrasado h\u00e1 " + late + (late == 1 ? " dia" : " dias");
    }
    private void showListScreen() {
        if (selectedIndex >= 0 && selectedIndex < lists.size()) {
            updateAutoLockedList(lists.get(selectedIndex));
        }
        homeTab = 0;
        buildRoot();
        ShoppingList list = lists.get(selectedIndex);
        addTopHeader(list.name, listSubtitle(list), true);
        if (list.locked) {
            String message = list.archived
                    ? "Esta lista esta no historico. Voce pode copiar ou remover, mas ela permanece protegida."
                    : "Desbloqueie pelo cadeado para editar esta lista.";
            root.addView(infoCard("Lista protegida", message), matchWrapWithTop(dp(10)));
        }
        if (list.budget > 0) addListBudgetCard(list);

        addSearchBar("Pesquisar itens da lista", listSearch, value -> {
            listSearch = value;
            showListScreen();
        });
        addItems();
        setContentView(rootScroll());
    }

    private void buildRoot() {
        applySystemBars();
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), statusBarHeight() + dp(20), dp(18), dp(22));
    }

    private View rootScroll() {
        FrameLayout shell = new FrameLayout(this);
        shell.setBackgroundColor(screenBg());

        ImageView background = new ImageView(this);
        background.setImageResource(backgroundForCurrentScreen());
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        background.setAlpha(1f);
        shell.addView(background, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);
        scrollView.setPadding(0, 0, 0, keyboardAwareBottomPadding(0));
        scrollView.setBackgroundColor(Color.TRANSPARENT);
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        shell.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        String scrollKey = currentScrollKey();
        int savedScrollY = scrollPositions.containsKey(scrollKey) ? scrollPositions.get(scrollKey) : 0;
        configureKeyboardAwareScroll(shell, scrollView);
        addGlobalScrollJumpButtons(shell, scrollView, scrollKey);
        restoreScrollPosition(scrollView, savedScrollY);
        return shell;
    }

    private void configureKeyboardAwareScroll(FrameLayout shell, ScrollView scrollView) {
        final int[] lastBottomPadding = {-1};
        applyKeyboardAwarePadding(scrollView, 0, lastBottomPadding);

        if (Build.VERSION.SDK_INT >= 30) {
            shell.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets ime = insets.getInsets(WindowInsets.Type.ime());
                applyKeyboardAwarePadding(scrollView, ime.bottom, lastBottomPadding);
                if (ime.bottom > dp(120)) postScrollFocusedIntoView(scrollView);
                return insets;
            });
        }

        shell.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect visible = new Rect();
            shell.getWindowVisibleDisplayFrame(visible);
            int hiddenBottom = Math.max(0, shell.getRootView().getHeight() - visible.bottom);
            int keyboardHeight = hiddenBottom > dp(120) ? hiddenBottom : 0;
            applyKeyboardAwarePadding(scrollView, keyboardHeight, lastBottomPadding);
            if (keyboardHeight > 0) postScrollFocusedIntoView(scrollView);
        });

        shell.getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
            if (newFocus instanceof EditText || newFocus instanceof AutoCompleteTextView) {
                postScrollFocusedIntoView(scrollView);
            }
        });
    }

    private void applyKeyboardAwarePadding(ScrollView scrollView, int keyboardHeight, int[] lastBottomPadding) {
        int bottom = keyboardAwareBottomPadding(keyboardHeight);
        if (lastBottomPadding[0] == bottom) return;
        lastBottomPadding[0] = bottom;
        scrollView.setPadding(scrollView.getPaddingLeft(), scrollView.getPaddingTop(), scrollView.getPaddingRight(), bottom);
    }

    private int keyboardAwareBottomPadding(int keyboardHeight) {
        return keyboardHeight > dp(120) ? keyboardHeight + dp(72) : dp(160);
    }

    private void postScrollFocusedIntoView(ScrollView scrollView) {
        scrollView.postDelayed(() -> scrollFocusedIntoView(scrollView), 80);
        scrollView.postDelayed(() -> scrollFocusedIntoView(scrollView), 240);
    }

    private void scrollFocusedIntoView(ScrollView scrollView) {
        View focused = getCurrentFocus();
        if (focused == null || !isDescendantOf(focused, scrollView)) return;

        Rect rect = new Rect();
        focused.getDrawingRect(rect);
        scrollView.offsetDescendantRectToMyCoords(focused, rect);

        int extra = dp(24);
        int visibleHeight = Math.max(dp(90), scrollView.getHeight() - scrollView.getPaddingBottom() - extra);
        int currentY = scrollView.getScrollY();
        int wantedY = currentY;
        if (rect.bottom + extra > currentY + visibleHeight) {
            wantedY = rect.bottom + extra - visibleHeight;
        } else if (rect.top - extra < currentY) {
            wantedY = rect.top - extra;
        }
        scrollView.smoothScrollTo(0, Math.max(0, wantedY));
    }

    private boolean isDescendantOf(View child, ViewGroup parent) {
        View current = child;
        while (current != null) {
            if (current == parent) return true;
            android.view.ViewParent next = current.getParent();
            if (!(next instanceof View)) return false;
            current = (View) next;
        }
        return false;
    }

    private String currentScrollKey() {
        if (homeTab == 5) return "comparison:" + currentListIdForKey();
        if (homeTab == 6) return "stock-history";
        if (homeTab == 9) return "budget:" + selectedBudgetMonthKey();
        if (homeTab == 3) return "history";
        if (homeTab == 2) return "spending:" + spendingRangeMonths;
        if (homeTab == 1) return "stock";
        if (selectedIndex >= 0) return "list:" + currentListIdForKey();
        return "home";
    }

    private String currentListIdForKey() {
        if (selectedIndex >= 0 && selectedIndex < lists.size()) {
            ShoppingList list = lists.get(selectedIndex);
            return isBlank(list.id) ? String.valueOf(selectedIndex) : list.id;
        }
        return "none";
    }

    private void restoreScrollPosition(ScrollView scrollView, int savedScrollY) {
        if (savedScrollY <= 0) return;
        scrollView.post(() -> scrollToSavedPosition(scrollView, savedScrollY, false));
        scrollView.postDelayed(() -> scrollToSavedPosition(scrollView, savedScrollY, false), 180);
    }

    private void scrollToSavedPosition(ScrollView scrollView, int savedScrollY, boolean smooth) {
        int maxScroll = maxScrollY(scrollView);
        int target = Math.max(0, Math.min(savedScrollY, maxScroll));
        if (smooth) scrollView.smoothScrollTo(0, target);
        else scrollView.scrollTo(0, target);
    }

    private void addGlobalScrollJumpButtons(FrameLayout shell, ScrollView scrollView, String scrollKey) {
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setGravity(Gravity.CENTER);
        controls.setVisibility(View.GONE);

        ImageButton up = scrollJumpButton(R.drawable.ic_arrow_up);
        ImageButton down = scrollJumpButton(R.drawable.ic_arrow_down);
        up.setOnClickListener(v -> scrollView.smoothScrollTo(0, 0));
        down.setOnClickListener(v -> scrollView.smoothScrollTo(0, maxScrollY(scrollView)));

        controls.addView(up, new LinearLayout.LayoutParams(dp(50), dp(50)));
        LinearLayout.LayoutParams downParams = new LinearLayout.LayoutParams(dp(50), dp(50));
        downParams.setMargins(0, dp(8), 0, 0);
        controls.addView(down, downParams);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.END
        );
        params.setMargins(0, 0, dp(16), dp(28));
        shell.addView(controls, params);

        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            scrollPositions.put(scrollKey, scrollY);
            updateGlobalScrollJumpButtons(scrollView, controls, up, down);
        });
        scrollView.post(() -> updateGlobalScrollJumpButtons(scrollView, controls, up, down));
        scrollView.postDelayed(() -> updateGlobalScrollJumpButtons(scrollView, controls, up, down), 240);
    }

    private ImageButton scrollJumpButton(int iconRes) {
        int bg = isDarkTheme() ? Color.argb(224, 15, 23, 42) : Color.argb(235, 255, 255, 255);
        int fg = isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText();
        ImageButton button = new ImageButton(this);
        button.setImageResource(iconRes);
        button.setColorFilter(fg);
        button.setBackground(round(bg, dp(999), blend(accent(), stroke(), isDarkTheme() ? 0.72f : 0.35f), 1));
        button.setPadding(dp(12), dp(12), dp(12), dp(12));
        button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        button.setAlpha(0.92f);
        elevate(button, 9);
        return button;
    }

    private void updateGlobalScrollJumpButtons(ScrollView scrollView, LinearLayout controls, ImageButton up, ImageButton down) {
        int maxScroll = maxScrollY(scrollView);
        boolean hugeContent = maxScroll > Math.max(dp(520), scrollView.getHeight());
        if (!hugeContent) {
            controls.setVisibility(View.GONE);
            return;
        }
        int y = scrollView.getScrollY();
        boolean canUp = y > dp(96);
        boolean canDown = y < maxScroll - dp(96);
        up.setVisibility(canUp ? View.VISIBLE : View.GONE);
        down.setVisibility(canDown ? View.VISIBLE : View.GONE);
        controls.setVisibility((canUp || canDown) ? View.VISIBLE : View.GONE);
    }

    private int maxScrollY(ScrollView scrollView) {
        if (scrollView.getChildCount() == 0) return 0;
        View child = scrollView.getChildAt(0);
        int visible = Math.max(1, scrollView.getHeight() - scrollView.getPaddingBottom());
        return Math.max(0, child.getHeight() - visible);
    }

    private int backgroundForCurrentScreen() {
        boolean dark = isDarkTheme();
        if (selectedIndex >= 0 || homeTab == 5) return dark ? R.drawable.bg_list_dark : R.drawable.bg_list_light;
        if (homeTab == 1 || homeTab == 6) return dark ? R.drawable.bg_stock_dark : R.drawable.bg_stock_light;
        if (homeTab == 2 || homeTab == 9) return dark ? R.drawable.bg_spending_dark : R.drawable.bg_spending_light;
        if (homeTab == 3) return dark ? R.drawable.bg_history_dark : R.drawable.bg_history_light;
        return dark ? R.drawable.bg_home_dark : R.drawable.bg_home_light;
    }

    private void addTopHeader(String heading, String subheading, boolean listOpen) {
        boolean homeHeader = !listOpen && homeTab == 0;
        addBrandBanner(homeHeader);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(22), homeHeader ? dp(16) : dp(18), dp(22), dp(18));
        header.setBackground(neonHome() ? CheckMercadoNeonUi.panel(this) : glassPanelBg());
        elevate(header, 8);
        root.addView(header, matchWrap());

        LinearLayout topLine = new LinearLayout(this);
        topLine.setOrientation(LinearLayout.HORIZONTAL);
        topLine.setGravity(homeHeader ? Gravity.TOP : Gravity.CENTER_VERTICAL);
        header.addView(topLine, matchWrap());

        int sideButtonSize = homeHeader ? homeButtonSize() : dp(48);
        int iconColor = isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText();

        if (homeHeader) {
            ImageButton qr = homeImageIconButton(R.drawable.ic_qr_scan, accent(), isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent());
            if (neonHome()) applyNeonIconButton(qr, CheckMercadoNeonUi.GREEN);
            qr.setPadding(dp(14), dp(14), dp(14), dp(14));
            qr.setOnClickListener(v -> startFiscalQrScan());
            topLine.addView(qr, new LinearLayout.LayoutParams(sideButtonSize, sideButtonSize));
        }

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setGravity(homeHeader ? Gravity.TOP : Gravity.CENTER_VERTICAL);
        titleBlock.setPadding(0, 0, dp(10), 0);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        if (homeHeader) titleParams.setMargins(dp(12), 0, dp(10), 0);
        topLine.addView(titleBlock, titleParams);

        LinearLayout sideControls = new LinearLayout(this);
        sideControls.setOrientation(LinearLayout.VERTICAL);
        sideControls.setGravity(Gravity.CENTER_HORIZONTAL);
        sideControls.setPadding(0, dp(6), 0, 0);

        if (homeHeader) {
            ImageButton menu = homeImageIconButton(R.drawable.ic_menu, menuButtonBg(), isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText());
            if (neonHome()) applyNeonIconButton(menu, CheckMercadoNeonUi.BLUE);
            menu.setPadding(dp(14), dp(14), dp(14), dp(14));
            menu.setOnClickListener(this::showHomeMenu);
            sideControls.addView(menu, new LinearLayout.LayoutParams(sideButtonSize, sideButtonSize));
        } else {
            ImageButton themeTop = imageIconButton(isDarkTheme() ? R.drawable.ic_sun : R.drawable.ic_moon,
                    accent(),
                    isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText());
            themeTop.setOnClickListener(v -> toggleTheme());
            sideControls.addView(themeTop, new LinearLayout.LayoutParams(sideButtonSize, sideButtonSize));
        }

        topLine.addView(sideControls, new LinearLayout.LayoutParams(sideButtonSize + dp(4), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText(heading);
        title.setTextColor(neonHome() ? CheckMercadoNeonUi.TEXT : primaryText());
        title.setTextSize(homeHeader ? 30 : 28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(subheading);
        subtitle.setTextColor(neonHome() ? CheckMercadoNeonUi.MUTED : mutedText());
        subtitle.setTextSize(14);
        subtitle.setPadding(0, dp(4), 0, 0);
        titleBlock.addView(subtitle);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        header.addView(actions, matchWrapWithTop(dp(14)));

        if (listOpen) {
            ShoppingList current = lists.get(selectedIndex);
            int listActionSize = listActionButtonSize(current.locked);
            LinearLayout strip = iconStrip();
            actions.addView(strip, matchHeight(listActionSize + dp(10)));

            addWeightedStripIcon(strip, R.drawable.ic_back, isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText(), true, v -> {
                selectedIndex = -1;
                if (selectedFromHistory) {
                    selectedFromHistory = false;
                    showHistoryScreen();
                } else {
                    showHomeScreen();
                }
            });

            if (!current.locked) {
                addWeightedStripIcon(strip, R.drawable.ic_plus, isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent(), true, v -> promptAddItem());
            }

            boolean canShare = selectedIndex >= 0 && !lists.get(selectedIndex).items.isEmpty();
            addWeightedStripIcon(strip, R.drawable.ic_share_nodes, isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent(), canShare, v -> {
                if (canShare) shareSelectedList();
            });

            addWeightedStripIcon(strip, R.drawable.ic_print, isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText(), true, v -> showPrintPreview());

            addWeightedStripIcon(strip, sortIcon(current.sortMode), isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText(), !current.locked, v -> {
                if (current.locked) return;
                current.sortMode = (current.sortMode + 1) % 3;
                save();
                showListScreen();
            });

        } else {
            if (homeTab != 0) {
                LinearLayout strip = iconStrip();
                actions.addView(strip, new LinearLayout.LayoutParams(0, dp(54), 1));
                addWeightedStripIcon(strip, R.drawable.ic_back, isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText(), true, v -> showHomeScreen());
                if (homeTab == 1) {
                    addWeightedStripIcon(strip, R.drawable.ic_sort_keep_position, isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent(), true, v -> promptStockSort());
                }
            } else {
                actions.setOrientation(LinearLayout.VERTICAL);
                actions.setGravity(Gravity.CENTER_HORIZONTAL);
                int mainActionSize = homeMainActionSize(4);
                LinearLayout mainActions = iconStrip();
                actions.addView(mainActions, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mainActionSize + dp(10)));

                ImageButton newList = addStripIcon(mainActions, R.drawable.ic_cart, isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent(), true, mainActionSize, v -> promptNewList());
                newList.setOnClickListener(v -> promptNewList());

                ImageButton stockButton = addStripIcon(mainActions, R.drawable.ic_box, isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText(), true, mainActionSize, v -> showStockWindow(false));
                stockButton.setOnClickListener(v -> showStockWindow(false));
                stockButton.setOnLongClickListener(v -> {
                    if (secretLogoTaps >= 3) {
                        showMarketGame();
                        return true;
                    }
                    return false;
                });

                addStripIcon(mainActions, R.drawable.ic_history, isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText(), true, mainActionSize, v -> showHistoryScreen());
                addStripIcon(mainActions, R.drawable.ic_budget_chart, isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent(), true, mainActionSize, v -> showMonthlyBudgetScreen());
            }

        }
    }

    private void addBrandBanner(boolean homeHeader) {
        FrameLayout banner = new FrameLayout(this);
        banner.setBackgroundColor(Color.TRANSPARENT);

        ImageView brand = new ImageView(this);
        brand.setImageResource(isDarkTheme() ? R.drawable.brand_logo_dark : R.drawable.brand_logo_light);
        brand.setAdjustViewBounds(true);
        brand.setScaleType(ImageView.ScaleType.FIT_CENTER);
        brand.setPadding(dp(2), 0, dp(2), 0);
        brand.setOnClickListener(v -> {
            blinkBrandLogo(brand);
            registerSecretLogoTap();
        });
        banner.addView(brand, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        ));

        LinearLayout.LayoutParams params = matchHeight(homeHeader ? dp(116) : dp(96));
        params.setMargins(0, 0, 0, dp(10));
        root.addView(banner, params);
    }

    private void blinkBrandLogo(ImageView brand) {
        Handler handler = new Handler(Looper.getMainLooper());
        int steps = 15;
        for (int i = 0; i <= steps; i++) {
            final int step = i;
            handler.postDelayed(() -> {
                if (step >= steps) {
                    brand.setAlpha(1f);
                    return;
                }
                brand.setAlpha(step % 2 == 0 ? 0f : 1f);
            }, step * 100L);
        }
    }

    private void showHomeMenu(View anchor) {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(dp(8), dp(8), dp(8), dp(8));
        menu.setBackground(round(cardBg(), dp(16), stroke(), 1));

        int width = Math.min(dp(260), getResources().getDisplayMetrics().widthPixels - dp(36));
        final PopupWindow[] popupRef = new PopupWindow[1];
        addHomeMenuItem(menu, isDarkTheme() ? "Tema claro" : "Tema escuro", isDarkTheme() ? R.drawable.ic_sun : R.drawable.ic_moon, () -> {
            if (popupRef[0] != null) popupRef[0].dismiss();
            toggleTheme();
        });
        addHomeMenuItem(menu, "Cor do app", R.drawable.ic_palette, () -> {
            if (popupRef[0] != null) popupRef[0].dismiss();
            promptAccentColor();
        });
        addHomeMenuItem(menu, "Atualizar", R.drawable.ic_update, () -> {
            if (popupRef[0] != null) popupRef[0].dismiss();
            UpdateManager.checkForUpdates(this, true);
        });
        addHomeMenuItem(menu, "Backup", R.drawable.ic_backup, () -> {
            if (popupRef[0] != null) popupRef[0].dismiss();
            exportBackup();
        });
        addHomeMenuItem(menu, "Cr\u00e9ditos", R.drawable.ic_credits, () -> {
            if (popupRef[0] != null) popupRef[0].dismiss();
            showCredits();
        });

        PopupWindow popup = new PopupWindow(menu, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(round(cardBg(), dp(16), stroke(), 1));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) popup.setElevation(dp(10));
        popupRef[0] = popup;

        int xOffset = -(width - Math.max(anchor.getWidth(), homeButtonSize()));
        popup.showAsDropDown(anchor, xOffset, dp(8));
    }

    private void addHomeMenuItem(LinearLayout menu, String text, int iconRes, Runnable action) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(12), dp(10), dp(12), dp(10));
        item.setBackground(round(inputBg(), dp(12), Color.TRANSPARENT, 0));
        item.setOnClickListener(v -> action.run());

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(accent());
        item.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));

        TextView label = label(text, 15, true, primaryText());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        textParams.setMargins(dp(12), 0, 0, 0);
        item.addView(label, textParams);

        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, menu.getChildCount() == 0 ? 0 : dp(6), 0, 0);
        menu.addView(item, params);
    }

    private void addStockTabs() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4));
        tabs.setBackground(inputPanelBg(false));
        root.addView(tabs, matchHeightWithTop(dp(64), dp(12)));

        addStockTabButton(tabs, "Estoque", R.drawable.ic_box, 1, () -> showStockWindow(false));
        addStockTabDivider(tabs);
        addStockTabButton(tabs, "Gastos", R.drawable.ic_report, 2, () -> showStockWindow(true));
        addStockTabDivider(tabs);
        View history = addStockTabButton(tabs, "Historico", R.drawable.ic_history, 6, this::showStockHistoryWindow);
        if (stockHistoryPending && homeTab != 6) startStockHistoryBlink(history);

        LinearLayout tools = iconStrip();
        addWeightedStripIcon(tools, R.drawable.ic_undo,
                stockUndoStockJson == null ? mutedText() : (isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent()),
                stockUndoStockJson != null,
                v -> undoStockAction());
        root.addView(tools, matchHeightWithTop(dp(50), dp(8)));
    }

    private View addStockTabButton(LinearLayout tabs, String label, int iconRes, int tab, Runnable action) {
        boolean active = homeTab == tab;
        LinearLayout tabView = new LinearLayout(this);
        tabView.setOrientation(LinearLayout.HORIZONTAL);
        tabView.setGravity(Gravity.CENTER);
        tabView.setPadding(dp(8), 0, dp(8), 0);
        tabView.setClickable(true);
        tabView.setFocusable(true);
        tabView.setOnClickListener(v -> action.run());
        if (active) {
            tabView.setBackground(outlineButtonBg(isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent(), dp(14)));
        }

        int color = active
                ? (isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent())
                : (isDarkTheme() ? CheckMercadoNeonUi.MUTED : mutedText());
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(color);
        tabView.addView(icon, new LinearLayout.LayoutParams(dp(21), dp(21)));

        TextView text = label(label, 12, true, color);
        text.setSingleLine(true);
        text.setIncludeFontPadding(false);
        text.setPadding(dp(6), 0, 0, 0);
        tabView.addView(text);

        tabs.addView(tabView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return tabView;
    }

    private void addStockTabDivider(LinearLayout tabs) {
        View divider = new View(this);
        divider.setBackgroundColor(isDarkTheme() ? Color.argb(72, 45, 140, 255) : Color.argb(90, 148, 163, 184));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(1), dp(26));
        params.setMargins(dp(2), 0, dp(2), 0);
        tabs.addView(divider, params);
    }

    private void startStockHistoryBlink(View button) {
        button.post(new Runnable() {
            private boolean green = true;

            @Override
            public void run() {
                if (!stockHistoryPending || homeTab == 6 || button.getParent() == null) return;
                int bg = green ? Color.rgb(22, 163, 74) : Color.rgb(220, 38, 38);
                button.setBackground(isDarkTheme() ? CheckMercadoNeonUi.iconButton(MainActivity.this, bg) : glowRound(bg, dp(14)));
                green = !green;
                button.postDelayed(this, 650);
            }
        });
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
        actions.addView(reset, weighted());
        LinearLayout.LayoutParams scoreParams = weighted();
        scoreParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(scores, scoreParams);

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
        setContentView(rootScroll());
    }

    private void showCompraInvaders() {
        selectedIndex = -1;
        selectedFromHistory = false;
        homeTab = 7;
        buildRoot();
        addTopHeader("Compra Invaders", "Defenda a lista dos precos invasores.", false);

        LinearLayout topActions = new LinearLayout(this);
        topActions.setGravity(Gravity.CENTER);
        Button ranking = button("Ranking", accent(), Color.WHITE);
        topActions.addView(ranking, new LinearLayout.LayoutParams(dp(110), dp(50)));
        root.addView(topActions, matchWrapWithTop(dp(8)));

        TextView status = label("Toque em atirar e proteja o carrinho.", 14, false, mutedText());
        status.setGravity(Gravity.CENTER);
        root.addView(status, matchWrapWithTop(dp(8)));

        invadersView = new CompraInvadersView(this, status);
        root.addView(invadersView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(390)));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        root.addView(controls, matchWrapWithTop(dp(12)));

        Button left = iconButton("<", accent(), Color.WHITE);
        Button fire = button("Atirar", accent(), Color.WHITE);
        Button right = iconButton(">", accent(), Color.WHITE);
        controls.addView(left, new LinearLayout.LayoutParams(dp(64), dp(54)));
        LinearLayout.LayoutParams fireParams = new LinearLayout.LayoutParams(dp(92), dp(54));
        fireParams.setMargins(dp(8), 0, dp(8), 0);
        controls.addView(fire, fireParams);
        controls.addView(right, new LinearLayout.LayoutParams(dp(64), dp(54)));

        left.setOnClickListener(v -> invadersView.movePlayer(-1));
        right.setOnClickListener(v -> invadersView.movePlayer(1));
        fire.setOnClickListener(v -> invadersView.fire());
        ranking.setOnClickListener(v -> showInvadersRanking());
        setContentView(rootScroll());
        invadersView.start();
    }

    private void showInvadersRanking() {
        int best = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_INVADERS_BEST, 0);
        dialog()
                .setTitle("Ranking")
                .setMessage("Hi-score: " + best + " pontos")
                .setPositiveButton("Fechar", null)
                .show();
    }

    private void showStockHistoryWindow() {
        selectedIndex = -1;
        selectedFromHistory = false;
        homeTab = 6;
        clearStockHistoryPending();
        buildRoot();
        addTopHeader("Historico", "Baixas do estoque guardadas para consulta.", false);
        addStockTabs();
        addSearchBar("Pesquisar historico do estoque", stockHistorySearch, value -> {
            stockHistorySearch = value;
            showStockHistoryWindow();
        });
        addStockHistoryScreen();
        setContentView(rootScroll());
    }

    private View listCard(int index) {
        ShoppingList list = lists.get(index);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(12), dp(12));
        int listColor = list.displayColor();
        boolean neon = isDarkTheme();
        int accentColor = listColor == 0 ? accent() : listColor;
        card.setBackground(neon ? CheckMercadoNeonUi.card(this, accentColor) : glassCardBg(listColor));
        elevate(card, 7);
        if (list.id != null && list.id.equals(flashImportedListId)) {
            flashImportedListId = "";
            card.post(() -> flashImportedListCard(card, listColor));
        }
        card.setOnClickListener(v -> {
            selectedIndex = index;
            selectedFromHistory = list.archived;
            showListScreen();
        });
        card.setOnLongClickListener(v -> {
            showListOptions(index);
            return true;
        });

        FrameLayout iconFrame = new FrameLayout(this);
        iconFrame.setBackground(isDarkTheme()
                ? CheckMercadoNeonUi.chip(this, CheckMercadoNeonUi.GREEN)
                : round(withAlpha(accentColor, 24), dp(16), withAlpha(accentColor, 165), 1));
        ImageView listIcon = new ImageView(this);
        listIcon.setImageResource(R.drawable.ic_clipboard_list);
        listIcon.setColorFilter(isDarkTheme() ? CheckMercadoNeonUi.GREEN : accentColor);
        iconFrame.addView(listIcon, new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.CENTER));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(58), dp(58));
        iconParams.setMargins(0, 0, dp(12), 0);
        card.addView(iconFrame, iconParams);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(content, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView name = new TextView(this);
        name.setText(list.name);
        name.setTextSize(19);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setMaxLines(2);
        name.setTextColor(neon ? CheckMercadoNeonUi.TEXT : (listColor == 0 ? primaryText() : readableOnTint(listColor)));
        content.addView(name, matchWrap());

        LinearLayout meta = iconText(R.drawable.ic_calendar_tiny,
                formatShortDate(list.createdAt) + " - " + list.items.size() + " itens - " + completedCount(list) + " concluidos",
                13, false, neon ? CheckMercadoNeonUi.MUTED : mutedText(), neon ? CheckMercadoNeonUi.MUTED : mutedText());
        meta.setPadding(0, dp(5), 0, 0);
        content.addView(meta, matchWrap());

        TextView total = label("Total " + money.format(totalOfList(list)), 13, true,
                neon ? CheckMercadoNeonUi.GREEN : readableOnTint(accentColor));
        total.setPadding(dp(10), dp(4), dp(10), dp(4));
        total.setBackground(softPillBg(neon ? CheckMercadoNeonUi.GREEN : accentColor));
        LinearLayout.LayoutParams totalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        totalParams.setMargins(0, dp(7), 0, 0);
        content.addView(total, totalParams);
        if (list.budget > 0) {
            TextView budget = label("Dispon\u00edvel " + money.format(list.budget - totalOfList(list)), 12, true, budgetStatusColor(list));
            budget.setPadding(dp(10), dp(4), dp(10), dp(4));
            budget.setBackground(softPillBg(budgetStatusColor(list)));
            LinearLayout.LayoutParams budgetParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            budgetParams.setMargins(0, dp(5), 0, 0);
            content.addView(budget, budgetParams);
        }

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(dp(48), ViewGroup.LayoutParams.WRAP_CONTENT);
        rightParams.setMargins(dp(8), 0, 0, 0);
        card.addView(right, rightParams);

        int lockColor = list.locked ? Color.rgb(225, 29, 72) : Color.rgb(22, 163, 74);
        ImageButton lock = imageIconButton(list.locked ? R.drawable.ic_lock_closed : R.drawable.ic_lock_open,
                lockColor,
                isDarkTheme() ? Color.WHITE : lockColor);
        if (neon) applyNeonIconButton(lock, list.locked ? CheckMercadoNeonUi.DANGER : CheckMercadoNeonUi.GREEN);
        lock.setOnClickListener(v -> {
            toggleListLock(list);
            if (!list.locked) showHomeTab();
        });
        lock.setEnabled(!(list.archived && list.locked));
        lock.setAlpha(list.archived && list.locked ? 0.45f : 1.0f);
        right.addView(lock, new LinearLayout.LayoutParams(dp(42), dp(42)));

        ImageButton more = moreMenuButton(neon ? CheckMercadoNeonUi.MUTED : mutedText());
        more.setOnClickListener(v -> showListOptions(index));
        LinearLayout.LayoutParams moreParams = new LinearLayout.LayoutParams(dp(46), dp(42));
        moreParams.setMargins(0, dp(5), 0, 0);
        right.addView(more, moreParams);
        return card;
    }

    private void flashImportedListCard(View target, int listColor) {
        int[] colors = new int[]{Color.rgb(37, 99, 235), Color.WHITE, Color.rgb(220, 38, 38)};
        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < 15; i++) {
            final int step = i;
            handler.postDelayed(() -> target.setBackground(glassCardBg(colors[step % colors.length])), step * 250L);
        }
        handler.postDelayed(() -> target.setBackground(isDarkTheme()
                ? CheckMercadoNeonUi.card(this, listColor == 0 ? accent() : listColor)
                : glassCardBg(listColor)), 15 * 250L);
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

    private int budgetStatusColor(ShoppingList list) {
        if (list == null || list.budget <= 0) return accent();
        double used = totalOfList(list);
        double ratio = used / list.budget;
        if (ratio >= 1.0) return Color.rgb(225, 29, 72);
        if (ratio >= 0.85) return Color.rgb(234, 179, 8);
        return Color.rgb(22, 163, 74);
    }

    private void addListBudgetCard(ShoppingList list) {
        double used = totalOfList(list);
        double available = list.budget - used;
        int color = budgetStatusColor(list);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(glassCardBg(0));
        elevate(card, 4);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top, matchWrap());
        top.addView(label("Or\u00e7amento da lista", 18, true, primaryText()), weighted());
        TextView status = label(available >= 0 ? "Dentro do limite" : "Ultrapassado", 13, true, color);
        status.setPadding(dp(10), dp(4), dp(10), dp(4));
        status.setBackground(softPillBg(color));
        top.addView(status);

        TextView line = label("Or\u00e7amento: " + money.format(list.budget)
                + "\nGasto: " + money.format(used)
                + "\nDispon\u00edvel: " + money.format(available), 14, true, mutedText());
        line.setPadding(0, dp(8), 0, 0);
        card.addView(line);
        card.addView(progressBarView(list.budget <= 0 ? 0 : used / list.budget, color), matchHeightWithTop(dp(10), dp(10)));

        root.addView(card, matchWrapWithTop(dp(10)));
    }

    private View progressBarView(double ratio, int color) {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackground(round(isDarkTheme() ? Color.argb(92, 15, 23, 42) : Color.argb(120, 226, 232, 240), dp(8), stroke(), 1));
        View fill = new View(this);
        fill.setBackground(round(color, dp(8), Color.TRANSPARENT, 0));
        frame.post(() -> {
            int width = frame.getWidth();
            int fillWidth = Math.max(dp(4), (int) (width * Math.min(1.0, Math.max(0.0, ratio))));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(fillWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            frame.updateViewLayout(fill, params);
        });
        frame.addView(fill, new FrameLayout.LayoutParams(dp(4), ViewGroup.LayoutParams.MATCH_PARENT));
        return frame;
    }

    private void addNeonChip(LinearLayout row, String text, int color) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextSize(12);
        chip.setTypeface(Typeface.DEFAULT_BOLD);
        chip.setTextColor(CheckMercadoNeonUi.TEXT);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(10), 0, dp(10), 0);
        chip.setBackground(CheckMercadoNeonUi.chip(this, color));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(30));
        params.setMargins(0, 0, dp(7), 0);
        row.addView(chip, params);
    }

    private int completedCount(ShoppingList list) {
        int done = 0;
        for (ShoppingItem item : list.items) {
            if (item.checked) done++;
        }
        return done;
    }

    private double totalOfList(ShoppingList list) {
        double total = 0;
        for (ShoppingItem item : list.items) {
            if (item.price > 0) total += item.price * quantityOf(item);
        }
        return total;
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
            if (!list.deletedFromHistory && list.archived == archived) return true;
        }
        return false;
    }

    private void toggleListLock(ShoppingList list) {
        if (list.locked) {
            if (list.archived) {
                Toast.makeText(this, "Listas no historico permanecem protegidas.", Toast.LENGTH_SHORT).show();
                return;
            }
            list.locked = false;
            list.archived = false;
            list.lockedAt = 0;
        } else {
            list.locked = true;
            list.lockedAt = System.currentTimeMillis();
            addSpendingRecordsForList(list);
            if (isListFinished(list)) askArchiveLockedList(list);
        }
        save();
    }

    private boolean isListFinished(ShoppingList list) {
        if (list.items.isEmpty()) return false;
        for (ShoppingItem item : list.items) {
            if (!item.checked) return false;
        }
        return true;
    }

    private void askArchiveLockedList(ShoppingList list) {
        dialog()
                .setTitle("Enviar para historico?")
                .setMessage("A lista foi protegida. Deseja remove-la da tela principal e guardar no historico?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    list.archived = true;
                    save();
                    showHomeTab();
                })
                .setNegativeButton("Nao", (dialog, which) -> showHomeTab())
                .show();
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
            TextView line = printText("\u2022 " + item.name + "\n  " + formatQty(qty) + " x " + unitPrice + " (" + total + ")", 15, false);
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

        TextView title = printText("Relat\u00f3rio de gastos", 22, true);
        title.setGravity(Gravity.CENTER);
        page.addView(title, matchWrap());
        page.addView(printText("Per\u00edodo: " + spendingRangeLabel(), 14, false), matchWrapWithTop(dp(12)));
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
            page.addView(printText("Sem dados no per\u00edodo.", 14, false), matchWrapWithTop(dp(4)));
        } else {
            for (Map.Entry<String, Double> row : categoryRows) {
                page.addView(printText("\u2022 " + row.getKey() + ": " + money.format(row.getValue()), 14, false), matchWrapWithTop(dp(4)));
            }
        }

        page.addView(printText("\nProdutos", 18, true), matchWrap());
        List<SpendingProduct> productRows = new ArrayList<>(products.values());
        Collections.sort(productRows, (a, b) -> Double.compare(b.total, a.total));
        int limit = Math.min(10, productRows.size());
        if (limit == 0) {
            page.addView(printText("Sem dados no per\u00edodo.", 14, false), matchWrapWithTop(dp(4)));
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

        StringBuilder html = printHtmlStart("Relat\u00f3rio de gastos");
        html.append("<h1>Relat\u00f3rio de gastos</h1>");
        html.append("<p>Per\u00edodo: ").append(escapeHtml(spendingRangeLabel())).append("</p>");
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
            html.append("<p>Sem dados no per\u00edodo.</p>");
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
            html.append("<p>Sem dados no per\u00edodo.</p>");
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
        stockSelectionStatus = null;
        if (stock.isEmpty()) {
            selectedStockIds.clear();
            root.addView(infoCard("Estoque vazio", "Marque itens comprados nas listas para adiciona-los ao estoque."), matchWrapWithTop(dp(10)));
            return;
        }
        pruneSelectedStockIds();
        List<StockEntry> rows = new ArrayList<>(stock);
        for (int i = rows.size() - 1; i >= 0; i--) {
            if (!matchesStockSearch(rows.get(i), stockSearch) || !matchesStockCategoryFilter(rows.get(i), false)) rows.remove(i);
        }
        sortStockRows(rows);
        if (rows.isEmpty()) {
            root.addView(infoCard("Nada encontrado", "Nenhum item do estoque corresponde a pesquisa."), matchWrapWithTop(dp(10)));
            return;
        }
        for (StockEntry entry : rows) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));
            boolean selected = selectedStockIds.contains(entry.id);
            card.setOnLongClickListener(v -> {
                showStockOptions(entry);
                return true;
            });
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            card.addView(top, matchWrap());

            TextView name = label(entry.name, 18, true, primaryText());
            top.addView(name, weighted());

            ImageButton more = moreMenuButton(mutedText());
            more.setOnClickListener(v -> showStockOptions(entry));
            top.addView(more, new LinearLayout.LayoutParams(dp(46), dp(42)));

            TextView marker = label(selected ? "Selecionado" : "", 13, true, accent());
            marker.setPadding(0, selected ? dp(3) : 0, 0, 0);
            card.addView(marker);
            applyStockSelectionState(card, marker, selected);
            card.setOnClickListener(v -> toggleStockSelection(entry, card, marker));
            String price = entry.price > 0 ? money.format(entry.price) : "sem preco";
            String total = entry.price > 0 ? money.format(entry.price * entry.quantity) : "sem preco";
            TextView meta = label(formatStockQuantity(entry) + " x " + price + " (" + total + ")", 14, true, mutedText());
            meta.setPadding(0, dp(4), 0, 0);
            card.addView(meta);
            LinearLayout duration = iconText(R.drawable.ic_calendar_tiny, formatStockAge(entry), 14, false, accent(), accent());
            duration.setPadding(0, dp(5), 0, 0);
            card.addView(duration);
            LinearLayout category = iconText(R.drawable.ic_tag_tiny, "Categoria: " + categoryOf(entry), 13, false, mutedText(), mutedText());
            category.setPadding(0, dp(4), 0, 0);
            card.addView(category);
            if (entry.updatedAt > entry.addedAt + 1000L && entry.consumedAt <= 0) {
                LinearLayout edited = iconText(R.drawable.ic_calendar_tiny, "Editado: " + formatDateTime(entry.updatedAt), 13, false, mutedText(), mutedText());
                edited.setPadding(0, dp(4), 0, 0);
                card.addView(edited);
            }
            root.addView(card, matchWrapWithTop(dp(10)));
        }
    }

    private void updateStockSelectionStatus() {
        // A selecao aparece no proprio item; nao exibimos uma faixa extra no estoque.
    }

    private void toggleStockSelection(StockEntry entry, LinearLayout card, TextView marker) {
        if (entry == null || entry.id == null) return;
        if (selectedStockIds.contains(entry.id)) {
            selectedStockIds.remove(entry.id);
        } else {
            selectedStockIds.add(entry.id);
        }
        applyStockSelectionState(card, marker, selectedStockIds.contains(entry.id));
        updateStockSelectionStatus();
    }

    private void applyStockSelectionState(LinearLayout card, TextView marker, boolean selected) {
        card.setBackground(selected ? selectedCardBg() : glassCardBg(0));
        if (marker != null) {
            marker.setText(selected ? "Selecionado" : "");
            marker.setPadding(0, selected ? dp(3) : 0, 0, 0);
        }
    }

    private void pruneSelectedStockIds() {
        LinkedHashSet<String> live = new LinkedHashSet<>();
        for (StockEntry entry : stock) live.add(entry.id);
        selectedStockIds.retainAll(live);
    }

    private List<StockEntry> selectedStockEntries() {
        List<StockEntry> rows = new ArrayList<>();
        for (StockEntry entry : stock) {
            if (selectedStockIds.contains(entry.id)) rows.add(entry);
        }
        return rows;
    }

    private void sortStockRows(List<StockEntry> rows) {
        Collections.sort(rows, (a, b) -> {
            switch (stockSortMode) {
                case STOCK_SORT_NAME:
                    return normalize(a.name).compareTo(normalize(b.name));
                case STOCK_SORT_QUANTITY:
                    return Double.compare(b.quantity, a.quantity);
                case STOCK_SORT_PRICE:
                    return Double.compare(b.price, a.price);
                case STOCK_SORT_CATEGORY:
                    return normalize(categoryOf(a)).compareTo(normalize(categoryOf(b)));
                case STOCK_SORT_DAYS:
                    return Long.compare(stockDays(b), stockDays(a));
                case STOCK_SORT_DATE:
                default:
                    return Long.compare(b.addedAt, a.addedAt);
            }
        });
    }

    private void promptStockSort() {
        String[] options = new String[]{"Data", "Nome", "Unidade", "Pre\u00e7o", "Categoria", "Dias"};
        int[] modes = new int[]{STOCK_SORT_DATE, STOCK_SORT_NAME, STOCK_SORT_QUANTITY, STOCK_SORT_PRICE, STOCK_SORT_CATEGORY, STOCK_SORT_DAYS};
        dialog()
                .setTitle("Ordenar estoque")
                .setItems(options, (dialog, which) -> {
                    stockSortMode = modes[which];
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_STOCK_SORT_MODE, stockSortMode).apply();
                    showStockWindow(false);
                })
                .show();
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
        root.addView(infoCardWithIcon("Resumo", "Total do per\u00edodo selecionado:\n" + money.format(sum), R.drawable.ic_money_circle, Color.rgb(57, 229, 108)), matchWrapWithTop(dp(10)));
        addMetricGrid(currentTotal, previousTotal, difference, average, forecast, biggestEntry, products);
        addGoalCard(currentTotal, forecast);
        addSpendingAlerts(currentTotal, forecast, products);
        addMonthlyBars(totals, max);
        addStockDurationInsights();
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
        Button report = button("Relat\u00f3rio", Color.rgb(51, 65, 85), Color.WHITE);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView text = (TextView) view;
                    text.setTextColor(primaryText());
                    text.setTextSize(14);
                    text.setTypeface(Typeface.DEFAULT_BOLD);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(cardBg());
                view.setPadding(dp(12), dp(10), dp(12), dp(10));
                if (view instanceof TextView) {
                    TextView text = (TextView) view;
                    text.setTextColor(primaryText());
                    text.setTextSize(16);
                }
                return view;
            }
        };
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

        LinearLayout tools = iconStrip();
        addStripIcon(tools, R.drawable.ic_target,
                monthlyGoal > 0 ? (isDarkTheme() ? CheckMercadoNeonUi.GREEN : Color.rgb(22, 163, 74)) : mutedText(),
                true, dp(44), v -> promptMonthlyGoal());
        addStripIcon(tools, R.drawable.ic_report, isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText(), true, dp(44), v -> showSpendingReportPreview());
        addStripIcon(tools, R.drawable.ic_trash, isDarkTheme() ? CheckMercadoNeonUi.DANGER : Color.rgb(225, 29, 72), true, dp(44), v -> confirmClearSpendingHistory());
        LinearLayout.LayoutParams toolParams = new LinearLayout.LayoutParams(dp(154), dp(48));
        toolParams.setMargins(dp(8), 0, 0, 0);
        card.addView(tools, toolParams);
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
                .setMessage("Isso apagar\u00e1 o hist\u00f3rico de gastos e n\u00e3o poder\u00e1 ser restaurado pelo app. Listas e estoque n\u00e3o ser\u00e3o apagados. Continuar?")
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
        CharSequence[] options = {"Salvar backup criptografado", "Restaurar backup"};
        dialog()
                .setTitle("Backup")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) saveEncryptedBackupFile();
                    else openEncryptedBackupFile();
                })
                .show();
    }

    public void openBackupFromUpdater() {
        exportBackup();
    }

    private void saveEncryptedBackupFile() {
        try {
            pendingBackupFileText = buildEncryptedBackupFileText();
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, backupFileName());
            startActivityForResult(intent, REQUEST_BACKUP_SAVE);
        } catch (Exception e) {
            Toast.makeText(this, "Nao foi possivel gerar o backup.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openEncryptedBackupFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_BACKUP_OPEN);
    }

    private void savePendingBackupToUri(Uri uri) {
        if (uri == null || pendingBackupFileText == null) return;
        try (OutputStream output = getContentResolver().openOutputStream(uri)) {
            if (output == null) throw new java.io.IOException("Destino indisponivel");
            output.write(pendingBackupFileText.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "Backup salvo.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Nao foi possivel salvar o backup.", Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreBackupFromUri(Uri uri) {
        try {
            String fileText = readText(getContentResolver().openInputStream(uri));
            String backupLink = decryptBackupFileText(fileText);
            String backup = extractBackup(backupLink);
            if (isBlank(backup)) backup = backupLink;
            promptImportBackupFile(cleanPayload(backup));
        } catch (Exception e) {
            Toast.makeText(this, "Backup invalido.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startFiscalQrScan() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        showFiscalQrScanner();
    }

    private void showFiscalQrScanner() {
        stopQrScanner();
        selectedIndex = -1;
        selectedFromHistory = false;
        homeTab = 8;
        applySystemBars();

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(Color.BLACK);
        screen.setPadding(0, statusBarHeight(), 0, 0);

        Button close = iconButton("X", Color.argb(190, 15, 23, 42), Color.WHITE);
        close.setOnClickListener(v -> {
            stopQrScanner();
            showHomeScreen();
        });
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dp(52), dp(52));
        closeParams.setMargins(dp(12), dp(8), 0, dp(8));
        screen.addView(close, closeParams);

        FrameLayout scannerFrame = new FrameLayout(this);
        qrScannerView = new QrScannerView(this, this::onFiscalQrRead);
        scannerFrame.addView(qrScannerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scannerFrame.addView(new QrFrameOverlay(this), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        screen.addView(scannerFrame, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(screen);
    }

    private void onFiscalQrRead(String result) {
        if (result == null || result.trim().isEmpty()) return;
        stopQrScanner();
        importFiscalNoteFromUrl(result.trim());
    }

    private void stopQrScanner() {
        if (qrScannerView != null) {
            qrScannerView.stop();
            qrScannerView = null;
        }
    }

    private void promptFiscalQrUrl() {
        EditText input = dialogInput("Cole o link da nota fiscal", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        dialog()
                .setTitle("Importar nota fiscal")
                .setMessage("Use o leitor do Google, copie o link aberto pela nota e cole aqui.")
                .setView(input)
                .setPositiveButton("Importar", (dialog, which) -> importFiscalNoteFromUrl(input.getText().toString()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void importFiscalNoteFromUrl(String rawUrl) {
        String url = rawUrl == null ? "" : rawUrl.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            showFiscalImportError("QR lido, mas nao parece ser um link valido.\n\nConteudo: " + previewErrorText(url));
            return;
        }
        showFiscalProcessingScreen();
        new Thread(() -> {
            String lastContent = "";
            String accessKey = extractFiscalAccessKeyFromUrl(url);
            Exception lastError = null;
            boolean blockedByCaptcha = false;
            for (String candidate : fiscalUrlCandidates(url)) {
                try {
                    lastContent = downloadText(candidate);
                    if (isFiscalCaptchaPage(lastContent)) {
                        blockedByCaptcha = true;
                        continue;
                    }
                    ShoppingList imported = parseFiscalNote(lastContent);
                    runOnUiThread(() -> saveFiscalList(imported));
                    return;
                } catch (Exception e) {
                    lastError = e;
                }
            }
            if (blockedByCaptcha || isSslTrustError(lastError)) {
                runOnUiThread(() -> showFiscalCaptchaScreen(url, accessKey));
                return;
            }
            String page = lastContent;
            String message = "Falha ao importar a nota.\n\nLink: " + previewErrorText(url);
            if (blockedByCaptcha) {
                message += "\n\nA SEFAZ abriu uma tela com captcha antes de liberar os itens da nota.";
                if (!isBlank(accessKey)) message += "\n\nChave lida: " + accessKey;
                message += "\n\nNão criei lista porque a página recebida não contém produtos.";
            } else if (lastError != null) {
                message += "\n\nErro: " + errorMessage(lastError);
            }
            String finalMessage = message;
            runOnUiThread(() -> showFiscalImportError(finalMessage, page));
        }).start();
    }

    private void showFiscalProcessingScreen() {
        stopQrScanner();
        selectedIndex = -1;
        selectedFromHistory = false;
        homeTab = 8;
        applySystemBars();

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setGravity(Gravity.CENTER);
        screen.setPadding(dp(26), statusBarHeight() + dp(20), dp(26), dp(26));
        screen.setBackgroundColor(screenBg());

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(26), dp(30), dp(26), dp(30));
        card.setBackground(round(cardBg(), dp(22), stroke(), 1));
        elevate(card, 6);

        ProgressBar progress = new ProgressBar(this);
        progress.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) {
            progress.setIndeterminateTintList(ColorStateList.valueOf(accent()));
        }
        card.addView(progress, new LinearLayout.LayoutParams(dp(62), dp(62)));

        TextView title = label("Processando nota", 22, true, primaryText());
        title.setGravity(Gravity.CENTER);
        card.addView(title, matchWrapWithTop(dp(18)));

        TextView detail = label("Lendo os dados da SEFAZ...", 15, false, mutedText());
        detail.setGravity(Gravity.CENTER);
        card.addView(detail, matchWrapWithTop(dp(8)));

        screen.addView(card, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(screen);
    }

    private void showFiscalCaptchaScreen(String url, String accessKey) {
        stopQrScanner();
        selectedIndex = -1;
        selectedFromHistory = false;
        homeTab = 8;
        applySystemBars();

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setPadding(dp(12), statusBarHeight() + dp(8), dp(12), dp(12));
        screen.setBackgroundColor(screenBg());

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        Button close = iconButton("X", Color.argb(190, 15, 23, 42), Color.WHITE);
        close.setTextSize(18);
        close.setOnClickListener(v -> showHomeScreen());
        toolbar.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView title = label("Confirmar NFC-e", 19, true, primaryText());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        titleParams.setMargins(dp(12), 0, 0, 0);
        toolbar.addView(title, titleParams);
        screen.addView(toolbar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        TextView help = label("Digite o codigo da SEFAZ. Quando os itens aparecerem, a lista sera salva automaticamente.", 14, false, mutedText());
        help.setPadding(dp(4), 0, dp(4), dp(8));
        screen.addView(help, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        boolean[] imported = {false};
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String currentUrl) {
                if (!isBlank(accessKey)) {
                    String js = "try{var k=document.getElementById('txt_chave_acesso');"
                            + "if(k&&!k.value){k.value='" + accessKey + "';}"
                            + "var c=document.getElementById('txt_cod_antirobo');if(c)c.focus();}catch(e){}";
                    view.evaluateJavascript(js, null);
                }
                view.postDelayed(() -> importFiscalHtmlFromWebView(view, imported), 450);
            }
        });
        screen.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(screen);
        webView.loadUrl(url.replace("|", "%7C"));
    }

    private void importFiscalHtmlFromWebView(WebView view, boolean[] imported) {
        if (view == null || imported[0]) return;
        view.evaluateJavascript("(function(){return document.documentElement.outerHTML;})()", value -> {
            if (imported[0]) return;
            try {
                String html = decodeJsString(value);
                if (isFiscalCaptchaPage(html)) return;
                ShoppingList list = parseFiscalNote(html);
                if (list.items.isEmpty()) return;
                imported[0] = true;
                saveFiscalList(list);
            } catch (Exception ignored) {
            }
        });
    }

    private String decodeJsString(String value) throws JSONException {
        if (value == null || "null".equals(value)) return "";
        return new JSONArray("[" + value + "]").getString(0);
    }

    private List<String> fiscalUrlCandidates(String rawUrl) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        String clean = rawUrl == null ? "" : rawUrl.trim().replace(" ", "%20");
        addFiscalUrlCandidate(candidates, clean);
        if (clean.startsWith("http://")) addFiscalUrlCandidate(candidates, "https://" + clean.substring(7));
        if (clean.startsWith("https://")) addFiscalUrlCandidate(candidates, "http://" + clean.substring(8));
        return new ArrayList<>(candidates);
    }

    private void addFiscalUrlCandidate(LinkedHashSet<String> candidates, String url) {
        if (isBlank(url)) return;
        candidates.add(url);
        candidates.add(url.replace("|", "%7C"));
    }

    private String downloadText(String link) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(link).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 Chrome/125 Safari/537.36");
        connection.setRequestProperty("Accept", "text/html,application/xml,text/xml,*/*");
        connection.setRequestProperty("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.6,en;q=0.5");
        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) throw new java.io.IOException("HTTP " + code);
        try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            return new String(output.toByteArray(), responseCharset(connection.getContentType()));
        } finally {
            connection.disconnect();
        }
    }

    private java.nio.charset.Charset responseCharset(String contentType) {
        if (contentType != null) {
            Matcher matcher = Pattern.compile("charset=([^;]+)", Pattern.CASE_INSENSITIVE).matcher(contentType);
            if (matcher.find()) {
                try {
                    return java.nio.charset.Charset.forName(matcher.group(1).trim());
                } catch (Exception ignored) {
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    private boolean isFiscalCaptchaPage(String html) {
        if (html == null) return false;
        String raw = html.toLowerCase(Locale.ROOT);
        return raw.contains("txt_chave_acesso")
                && (raw.contains("txt_cod_antirobo") || raw.contains("img_captcha") || raw.contains("anti_robo"));
    }

    private String extractFiscalAccessKeyFromUrl(String url) {
        if (url == null) return "";
        Matcher matcher = Pattern.compile("([0-9]{44})").matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }

    private ShoppingList parseFiscalNote(String content) throws Exception {
        String text = content == null ? "" : content.trim();
        ShoppingList fromXml = text.startsWith("<") ? parseFiscalXml(text) : null;
        if (fromXml != null && !fromXml.items.isEmpty()) return fromXml;
        ShoppingList fromHtml = parseFiscalHtml(text);
        if (fromHtml.items.isEmpty()) throw new JSONException("Nota sem itens");
        removeInvalidFiscalItems(fromHtml);
        if (fromHtml.items.isEmpty()) throw new JSONException("A pagina foi lida, mas nao encontrei linhas de produto validas.");
        return fromHtml;
    }

    private ShoppingList parseFiscalXml(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            String market = firstXmlText(doc, "xNome");
            ShoppingList list = new ShoppingList(isBlank(market) ? "Nota fiscal" : market);
            NodeList dets = doc.getElementsByTagName("det");
            for (int i = 0; i < dets.getLength(); i++) {
                Element det = (Element) dets.item(i);
                String name = firstChildText(det, "xProd");
                if (isBlank(name)) continue;
                double qty = parsePrice(firstChildText(det, "qCom"));
                double unitPrice = parsePrice(firstChildText(det, "vUnCom"));
                if (qty <= 0) qty = 1;
                ShoppingItem item = new ShoppingItem(cleanFiscalText(name), unitPrice, formatQty(qty));
                list.items.add(item);
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private ShoppingList parseFiscalHtml(String html) {
        String market = firstHtmlMatch(html,
                "id=[\"']u20[\"'][^>]*>(.*?)<",
                "class=[\"']txtTopo[\"'][^>]*>(.*?)<",
                "<h4[^>]*>(.*?)</h4>",
                "<title[^>]*>(.*?)</title>");
        market = cleanFiscalMarketName(market);
        ShoppingList list = new ShoppingList(isBlank(market) ? "Nota fiscal" : market);
        Matcher rowMatcher = Pattern.compile("(?is)<tr[^>]*>(.*?)</tr>").matcher(html);
        while (rowMatcher.find()) {
            String row = rowMatcher.group(1);
            if (looksLikeFiscalItemBlock(row)) addFiscalItemFromHtmlRow(list, row);
        }
        if (list.items.isEmpty()) {
            Matcher itemMatcher = Pattern.compile("(?is)txtTit[^>]*>(.*?)</span>(.*?)(?=txtTit|</table|</body)").matcher(html);
            while (itemMatcher.find()) {
                addFiscalItem(list, itemMatcher.group(1), itemMatcher.group(2));
            }
        }
        return list;
    }

    private void addFiscalItemFromHtmlRow(ShoppingList list, String row) {
        String name = firstHtmlMatch(row, "txtTit[^>]*>(.*?)</span>");
        addFiscalItem(list, name, row);
    }

    private void addFiscalItem(ShoppingList list, String rawName, String block) {
        String name = cleanFiscalText(rawName);
        if (isInvalidFiscalItemName(name, block)) return;
        double qty = firstNumber(block, "Qtde\\.?\\s*:?\\s*</?[^>]*>*\\s*([0-9.,]+)", "Quantidade\\s*:?\\s*([0-9.,]+)", "Rqtd[^>]*>.*?([0-9]+[0-9.,]*)");
        double unitPrice = firstNumber(block, "Vl\\.?\\s*Unit\\.?\\s*:?\\s*</?[^>]*>*\\s*([0-9.,]+)", "Valor\\s*Unit\\.?\\s*:?\\s*([0-9.,]+)", "RvlUnit[^>]*>.*?([0-9]+[0-9.,]*)");
        double total = firstNumber(block, "valor[^>]*>\\s*([0-9.,]+)", "Valor\\s*Total\\s*:?\\s*([0-9.,]+)");
        if (unitPrice <= 0 && total <= 0 && !looksLikeFiscalItemBlock(block)) return;
        if (qty <= 0) qty = 1;
        if (unitPrice <= 0 && total > 0) unitPrice = total / qty;
        ShoppingItem item = new ShoppingItem(name, unitPrice, formatQty(qty));
        list.items.add(item);
    }

    private boolean looksLikeFiscalItemBlock(String block) {
        String raw = normalize(block);
        String key = normalize(cleanFiscalText(block));
        return raw.contains("txttit") || raw.contains("rqtd") || raw.contains("rvlunit")
                || key.contains("vl unit") || key.contains("qtde") || key.contains("valor total");
    }

    private void removeInvalidFiscalItems(ShoppingList list) {
        for (int i = list.items.size() - 1; i >= 0; i--) {
            ShoppingItem item = list.items.get(i);
            if (isInvalidFiscalItemName(item.name, "")) list.items.remove(i);
        }
    }

    private boolean isInvalidFiscalItemName(String name, String block) {
        String key = normalize(name);
        String blockKey = normalize(cleanFiscalText(block));
        if (key.isEmpty() || key.contains("descricao")) return true;
        if (key.contains("function ") || blockKey.contains("function ")) return true;
        return key.contains("portal estadual") || key.contains("chave de acesso")
                || key.contains("codigo impresso") || key.contains("secretaria da fazenda")
                || key.contains("nota fiscal de consumidor") || key.matches("v\\.\\s*\\d+.*");
    }

    private String cleanFiscalMarketName(String market) {
        String name = cleanFiscalText(market);
        String key = normalize(name);
        if (key.contains("nota fiscal") || key.contains("consulta danfe")
                || key.contains("portal estadual") || key.contains("secretaria da fazenda")) {
            return "";
        }
        return name;
    }

    private String firstXmlText(Document doc, String tag) {
        NodeList nodes = doc.getElementsByTagName(tag);
        if (nodes.getLength() == 0 || nodes.item(0) == null) return "";
        return nodes.item(0).getTextContent();
    }

    private String firstChildText(Element element, String tag) {
        NodeList nodes = element.getElementsByTagName(tag);
        if (nodes.getLength() == 0 || nodes.item(0) == null) return "";
        return nodes.item(0).getTextContent();
    }

    private String firstHtmlMatch(String text, String... patterns) {
        for (String pattern : patterns) {
            Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(text);
            if (matcher.find()) return cleanFiscalText(matcher.group(1));
        }
        return "";
    }

    private double firstNumber(String text, String... patterns) {
        for (String pattern : patterns) {
            Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(text);
            if (matcher.find()) return parsePrice(cleanFiscalText(matcher.group(1)));
        }
        return 0;
    }

    private String cleanFiscalText(String value) {
        String text = htmlDecode(value == null ? "" : value.replaceAll("(?is)<[^>]+>", " "));
        return text.replaceAll("\\s+", " ").trim();
    }

    private String htmlDecode(String value) {
        return value.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }

    private void saveFiscalList(ShoppingList list) {
        if (list == null || list.items.isEmpty()) {
            showFiscalImportError("A nota foi acessada, mas nenhum item foi encontrado.");
            return;
        }
        lists.add(0, list);
        save();
        selectedIndex = -1;
        selectedFromHistory = false;
        flashImportedListId = list.id;
        showHomeScreen();
    }

    private void showFiscalImportError(String message) {
        showFiscalImportError(message, "");
    }

    private void showFiscalImportError(String message, String pageContent) {
        stopQrScanner();
        selectedIndex = -1;
        selectedFromHistory = false;
        homeTab = 0;
        buildRoot();
        addTopHeader("Suas listas", "Crie listas e compare precos salvos.", false);
        setContentView(rootScroll());
        String finalMessage = message;
        if (pageContent != null && !pageContent.trim().isEmpty()) {
            copyTextToClipboard("Estrutura da nota fiscal", previewDebugPage(pageContent));
            finalMessage += "\n\nA estrutura da pagina foi copiada para a area de transferencia.";
        }
        dialog()
                .setTitle("Erro ao importar nota")
                .setMessage(finalMessage)
                .setPositiveButton("OK", null)
                .show();
    }

    private String errorMessage(Exception e) {
        if (e == null) return "Erro desconhecido";
        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) return e.getClass().getSimpleName();
        return e.getClass().getSimpleName() + ": " + message;
    }

    private boolean isSslTrustError(Exception e) {
        Throwable current = e;
        while (current != null) {
            String name = current.getClass().getName();
            String message = current.getMessage();
            if (name.contains("SSLHandshakeException")
                    || name.contains("CertPathValidatorException")
                    || (message != null && (message.contains("Trust anchor") || message.contains("certification path")))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String previewErrorText(String text) {
        if (text == null) return "";
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 420 ? cleaned.substring(0, 420) + "..." : cleaned;
    }

    private String previewDebugPage(String text) {
        String cleaned = text == null ? "" : text.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 12000 ? cleaned.substring(0, 12000) : cleaned;
    }

    private void copyTextToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    private void addStockHistoryScreen() {
        LinearLayout actions = iconStrip();
        addWeightedStripIcon(actions, stockHistorySortDesc ? R.drawable.ic_sort_checked_bottom : R.drawable.ic_sort_checked_top,
                isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent(),
                !stockHistory.isEmpty(), v -> {
            stockHistorySortDesc = !stockHistorySortDesc;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_STOCK_HISTORY_SORT_DESC, stockHistorySortDesc).apply();
            showStockHistoryWindow();
        });
        addWeightedStripIcon(actions, R.drawable.ic_trash, isDarkTheme() ? CheckMercadoNeonUi.DANGER : Color.rgb(225, 29, 72),
                !stockHistory.isEmpty(), v -> confirmClearStockHistory());
        root.addView(actions, matchHeightWithTop(dp(54), dp(10)));

        if (stockHistory.isEmpty()) {
            root.addView(infoCard("Hist\u00f3rico vazio", "Itens baixados do estoque aparecem aqui."), matchWrapWithTop(dp(10)));
            return;
        }
        List<StockEntry> rows = new ArrayList<>(stockHistory);
        for (int i = rows.size() - 1; i >= 0; i--) {
            if (!matchesStockSearch(rows.get(i), stockHistorySearch) || !matchesStockCategoryFilter(rows.get(i), true)) rows.remove(i);
        }
        Collections.sort(rows, (a, b) -> Double.compare(stockDurationDays(a), stockDurationDays(b)));
        if (stockHistorySortDesc) Collections.reverse(rows);
        root.addView(infoCard("Ordena\u00e7\u00e3o por dura\u00e7\u00e3o", stockHistorySortDesc ? "Maior dura\u00e7\u00e3o primeiro." : "Menor dura\u00e7\u00e3o primeiro."), matchWrapWithTop(dp(10)));
        if (rows.isEmpty()) {
            root.addView(infoCard("Nada encontrado", "Nenhum item do historico do estoque corresponde a pesquisa."), matchWrapWithTop(dp(10)));
            return;
        }

        for (StockEntry entry : rows) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));
            card.setBackground(glassCardBg(0));
            elevate(card, 5);
            card.setOnLongClickListener(v -> {
                showStockHistoryOptions(entry);
                return true;
            });
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            card.addView(top, matchWrap());
            top.addView(label(entry.name, 18, true, primaryText()), weighted());
            ImageButton more = moreMenuButton(mutedText());
            more.setOnClickListener(v -> showStockHistoryOptions(entry));
            top.addView(more, new LinearLayout.LayoutParams(dp(46), dp(42)));
            String price = entry.price > 0 ? money.format(entry.price) : "sem preco";
            String total = entry.price > 0 ? money.format(entry.price * entry.quantity) : "sem preco";
            TextView meta = label(formatStockQuantity(entry) + " x " + price + " (" + total + ")", 14, true, mutedText());
            meta.setPadding(0, dp(4), 0, 0);
            card.addView(meta);
            TextView duration = label("Dura\u00e7\u00e3o: " + formatDurationDays(stockDurationDays(entry)), 14, true, accent());
            duration.setPadding(0, dp(5), 0, 0);
            card.addView(duration);
            LinearLayout category = iconText(R.drawable.ic_tag_tiny, "Categoria: " + categoryOf(entry), 13, false, mutedText(), mutedText());
            category.setPadding(0, dp(5), 0, 0);
            card.addView(category);
            LinearLayout dates = iconText(R.drawable.ic_calendar_tiny, "Entrada: " + formatDateTime(entry.addedAt) + "\nBaixa: " + formatDateTime(entry.consumedAt), 13, false, mutedText(), mutedText());
            dates.setPadding(0, dp(5), 0, 0);
            card.addView(dates);
            root.addView(card, matchWrapWithTop(dp(10)));
        }
    }

    private void showStockHistoryOptions(StockEntry entry) {
        dialog()
                .setTitle(entry.name)
                .setItems(new String[]{"Remover permanentemente"}, (dialog, which) -> confirmDeleteStockHistoryEntry(entry))
                .show();
    }

    private void confirmDeleteStockHistoryEntry(StockEntry entry) {
        dialog()
                .setTitle("Remover permanentemente?")
                .setMessage(entry.name)
                .setPositiveButton("Remover", (dialog, which) -> {
                    rememberStockUndo();
                    stockHistory.remove(entry);
                    saveStockHistory();
                    showStockHistoryWindow();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmClearStockHistory() {
        dialog()
                .setTitle("Limpar historico do estoque?")
                .setMessage("Isso removera permanentemente todas as baixas do historico de estoque. Continuar?")
                .setPositiveButton("Limpar", (dialog, which) -> {
                    rememberStockUndo();
                    stockHistory.clear();
                    saveStockHistory();
                    showStockHistoryWindow();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private JSONObject buildBackupJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("app", "Check Mercado");
        json.put("backupVersion", 1);
        json.put("createdAt", System.currentTimeMillis());

        JSONArray listArray = new JSONArray();
        for (ShoppingList list : lists) listArray.put(list.toJson());
        json.put("lists", listArray);

        JSONArray stockArray = new JSONArray();
        for (StockEntry entry : stock) stockArray.put(entry.toJson());
        json.put("stock", stockArray);

        JSONArray stockHistoryArray = new JSONArray();
        for (StockEntry entry : stockHistory) stockHistoryArray.put(entry.toJson());
        json.put("stockHistory", stockHistoryArray);

        JSONArray spendingArray = new JSONArray();
        for (SpendingRecord entry : spendingHistory) spendingArray.put(entry.toJson());
        JSONArray monthlyBudgetArray = new JSONArray();
        for (MonthlyBudgetEntry entry : monthlyBudgetEntries) monthlyBudgetArray.put(entry.toJson());
        json.put("monthlyBudgetEntries", monthlyBudgetArray);
        JSONArray monthlyIncomeArray = new JSONArray();
        for (MonthlyBudgetIncome income : monthlyBudgetIncomes) monthlyIncomeArray.put(income.toJson());
        json.put("monthlyBudgetIncomes", monthlyIncomeArray);


        json.put("spendingHistory", spendingArray);

        JSONObject settings = new JSONObject();
        settings.put("themeMode", themeMode);
        settings.put("accentColor", accentColor);
        settings.put("spendingRangeMonths", spendingRangeMonths);
        settings.put("monthlyGoal", monthlyGoal);
        settings.put("monthlyBudgetLimit", monthlyBudgetLimit);
        settings.put("gameBest", getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_GAME_BEST, ""));
        json.put("settings", settings);
        return json;
    }

    private void addMetricGrid(double currentTotal, double previousTotal, double difference, double average, double forecast, SpendingRecord biggestEntry, Map<String, SpendingProduct> products) {
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row1, matchWrapWithTop(dp(8)));
        row1.addView(metricCard("M\u00eas atual", money.format(currentTotal), accent(), R.drawable.ic_calendar_money, Color.rgb(57, 229, 108)), weighted());
        LinearLayout.LayoutParams right = weighted();
        right.setMargins(dp(8), 0, 0, 0);
        row1.addView(metricCard("M\u00eas anterior", money.format(previousTotal), primaryText(), R.drawable.ic_calendar_tiny, Color.rgb(45, 140, 255)), right);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row2, matchWrapWithTop(dp(8)));
        int diffColor = difference <= 0 ? Color.rgb(22, 163, 74) : Color.rgb(225, 29, 72);
        row2.addView(metricCard("Diferen\u00e7a", money.format(difference), diffColor, R.drawable.ic_trend_down, diffColor), weighted());
        LinearLayout.LayoutParams avgParams = weighted();
        avgParams.setMargins(dp(8), 0, 0, 0);
        row2.addView(metricCard("M\u00e9dia mensal", money.format(average), primaryText(), R.drawable.ic_chart_pie, Color.rgb(45, 140, 255)), avgParams);

        LinearLayout rowForecast = new LinearLayout(this);
        rowForecast.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(rowForecast, matchWrapWithTop(dp(8)));
        int forecastColor = forecast > previousTotal && previousTotal > 0 ? Color.rgb(225, 29, 72) : accent();
        rowForecast.addView(metricCard("Previs\u00e3o do m\u00eas", money.format(forecast), forecastColor, R.drawable.ic_arrow_up, forecastColor), weighted());
        LinearLayout.LayoutParams paceParams = weighted();
        paceParams.setMargins(dp(8), 0, 0, 0);
        String pace = previousTotal <= 0 ? "Sem compara\u00e7\u00e3o" : (forecast > previousTotal ? "Acima do m\u00eas anterior" : "Dentro do ritmo");
        rowForecast.addView(metricCard("Ritmo", pace, primaryText(), R.drawable.ic_gauge, Color.rgb(57, 229, 108)), paceParams);

        SpendingProduct mostBought = mostBoughtProduct(products);
        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row3, matchWrapWithTop(dp(8)));
        String biggest = biggestEntry == null ? "Sem dados" : biggestEntry.name + " - " + money.format(biggestEntry.price * biggestEntry.quantity);
        row3.addView(metricCard("Maior compra", biggest, primaryText(), R.drawable.ic_money_circle, Color.rgb(57, 229, 108)), weighted());
        LinearLayout.LayoutParams boughtParams = weighted();
        boughtParams.setMargins(dp(8), 0, 0, 0);
        String bought = mostBought == null ? "Sem dados" : mostBought.name + " - " + formatQty(mostBought.quantity) + " un";
        row3.addView(metricCard("Mais comprado", bought, primaryText(), R.drawable.ic_cart, Color.rgb(45, 140, 255)), boughtParams);
    }

    private View metricCard(String title, String value, int valueColor, int iconRes, int iconColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(glassCardBg(0));
        elevate(card, 2);

        FrameLayout iconFrame = new FrameLayout(this);
        iconFrame.setBackground(softPillBg(iconColor));
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(iconColor);
        iconFrame.addView(icon, new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        iconParams.setMargins(0, 0, dp(10), 0);
        card.addView(iconFrame, iconParams);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        card.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        texts.addView(label(title, 12, true, mutedText()));
        TextView valueView = label(value, 16, true, valueColor);
        valueView.setPadding(0, dp(5), 0, 0);
        valueView.setSingleLine(false);
        texts.addView(valueView, matchWrap());
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
        TextView forecastText = label("Previs\u00e3o: " + money.format(forecast), 13, false, mutedText());
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
            addAlertLine(card, "Previs\u00e3o acima da meta em " + money.format(forecast - monthlyGoal), Color.rgb(234, 88, 12));
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
            addAlertLine(card, product.name + " acima da m\u00e9dia em " + money.format(product.latestPrice - average), Color.rgb(234, 88, 12));
            count++;
        }
        if (count == 0) {
            addAlertLine(card, "Nenhum alerta importante no per\u00edodo selecionado.", Color.rgb(22, 163, 74));
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
        root.addView(label("Gastos por m\u00eas", 18, true, primaryText()), matchWrapWithTop(dp(16)));
        for (String key : totals.keySet()) {
            double value = totals.get(key);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackground(round(cardBg(), dp(14), stroke(), 1));
            card.addView(label(key + " \u00b7 " + money.format(value), 15, true, primaryText()));
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
            root.addView(infoCard("Sem dados", "Marque itens com pre\u00e7o para criar o ranking de gastos."), matchWrapWithTop(dp(8)));
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

    private void addStockDurationInsights() {
        Map<String, StockDurationStats> stats = new LinkedHashMap<>();
        for (StockEntry entry : stockHistory) {
            if (entry.consumedAt <= entry.addedAt || entry.quantity <= 0) continue;
            String key = normalize(entry.name);
            StockDurationStats row = stats.get(key);
            if (row == null) {
                row = new StockDurationStats(entry.name);
                stats.put(key, row);
            }
            double days = Math.max(0.04, (entry.consumedAt - entry.addedAt) / 86400000.0);
            row.cycles++;
            row.totalDays += days;
            row.totalQuantity += entry.quantity;
            row.lastDays = days;
            row.lastAt = entry.consumedAt;
            if (row.cycles == 1 || days < row.minDays) row.minDays = days;
            if (row.cycles == 1 || days > row.maxDays) row.maxDays = days;
        }

        root.addView(label("Duração do estoque", 18, true, primaryText()), matchWrapWithTop(dp(16)));
        if (stats.isEmpty()) {
            root.addView(infoCard("Sem baixas no estoque", "Quando você der baixa em itens do estoque, o app calcula quanto tempo cada produto durou e monta este gráfico."), matchWrapWithTop(dp(8)));
            return;
        }

        List<StockDurationStats> rows = new ArrayList<>(stats.values());
        Collections.sort(rows, (a, b) -> Double.compare(b.averageDays(), a.averageDays()));
        double max = 1;
        double totalAverage = 0;
        for (StockDurationStats row : rows) {
            max = Math.max(max, row.averageDays());
            totalAverage += row.averageDays();
        }
        double generalAverage = totalAverage / rows.size();
        root.addView(infoCard("Resumo de duração", "Média entre produtos: " + formatDurationDays(generalAverage)
                + "\nProdutos analisados: " + rows.size()), matchWrapWithTop(dp(8)));

        int limit = Math.min(8, rows.size());
        for (int i = 0; i < limit; i++) {
            StockDurationStats row = rows.get(i);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackground(round(cardBg(), dp(14), stroke(), 1));
            elevate(card, 2);

            card.addView(label((i + 1) + ". " + row.name, 15, true, primaryText()));
            TextView meta = label("Média " + formatDurationDays(row.averageDays())
                    + " - menor " + formatDurationDays(row.minDays)
                    + " - maior " + formatDurationDays(row.maxDays), 13, false, mutedText());
            meta.setPadding(0, dp(4), 0, 0);
            card.addView(meta, matchWrap());

            TextView detail = label(row.cycles + " baixa(s), " + formatQty(row.totalQuantity)
                    + " un consumidas - última baixa " + formatDateLabel(row.lastAt), 13, false, mutedText());
            detail.setPadding(0, dp(4), 0, 0);
            card.addView(detail, matchWrap());

            LinearLayout barBg = new LinearLayout(this);
            barBg.setBackground(round(inputBg(), dp(8), Color.TRANSPARENT, 0));
            LinearLayout bar = new LinearLayout(this);
            int color = row.averageDays() >= generalAverage ? Color.rgb(22, 163, 74) : Color.rgb(234, 88, 12);
            bar.setBackground(round(color, dp(8), Color.TRANSPARENT, 0));
            int width = Math.max(dp(10), (int) (getResources().getDisplayMetrics().widthPixels * 0.72 * (row.averageDays() / max)));
            barBg.addView(bar, new LinearLayout.LayoutParams(width, dp(14)));
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
        root.addView(label("Hist\u00f3rico de pre\u00e7os", 18, true, primaryText()), matchWrapWithTop(dp(16)));
        if (insights.isEmpty()) {
            root.addView(infoCard("Pouco hist\u00f3rico", "Quando um produto aparecer em compras diferentes, o app mostra m\u00ednimo, m\u00e9dio, m\u00e1ximo e varia\u00e7\u00e3o."), matchWrapWithTop(dp(8)));
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

            TextView range = label("M\u00edn. " + money.format(product.minPrice)
                    + " - M\u00e9dio " + money.format(average)
                    + " - M\u00e1x. " + money.format(product.maxPrice), 13, false, mutedText());
            range.setPadding(0, dp(5), 0, 0);
            card.addView(range, matchWrap());

            TextView latest = label("\u00daltimo: " + money.format(product.latestPrice)
                    + " em " + formatDateLabel(product.latestAt), 13, true, trendColor);
            latest.setPadding(0, dp(5), 0, 0);
            card.addView(latest, matchWrap());

            if (economy > 0) {
                TextView tip = label("Se comprar pelo menor hist\u00f3rico, economiza " + money.format(economy) + " por unidade.", 13, false, Color.rgb(22, 163, 74));
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
        if (spendingRangeMonths == 1) return "M\u00eas atual";
        return "\u00daltimos " + spendingRangeMonths + " meses";
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
        card.setBackground(neonHome() ? CheckMercadoNeonUi.card(this, CheckMercadoNeonUi.BLUE) : glassCardBg(0));
        elevate(card, 5);
        elevate(card, 2);
        card.addView(label(title, 18, true, neonHome() ? CheckMercadoNeonUi.TEXT : primaryText()));
        TextView b = label(body, 14, false, neonHome() ? CheckMercadoNeonUi.MUTED : mutedText());
        b.setPadding(0, dp(5), 0, 0);
        card.addView(b);
        return card;
    }

    private View infoCardWithIcon(String title, String body, int iconRes, int iconColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(glassCardBg(0));
        elevate(card, 5);

        FrameLayout iconFrame = new FrameLayout(this);
        iconFrame.setBackground(softPillBg(iconColor));
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(iconColor);
        iconFrame.addView(icon, new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.CENTER));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(56), dp(56));
        iconParams.setMargins(0, 0, dp(12), 0);
        card.addView(iconFrame, iconParams);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        card.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        texts.addView(label(title, 18, true, primaryText()));
        TextView b = label(body, 14, false, mutedText());
        b.setPadding(0, dp(5), 0, 0);
        texts.addView(b);
        return card;
    }

    private void addSearchBar(String hint, String value, SearchCallback callback) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(12), 0, dp(6), 0);
        box.setBackground(isDarkTheme()
                ? CheckMercadoNeonUi.input(this, filterActiveForCurrentScreen())
                : inputPanelBg(filterActiveForCurrentScreen()));
        row.addView(box, new LinearLayout.LayoutParams(0, dp(52), 1));

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint(hint);
        search.setText(value == null ? "" : value);
        search.setSelection(search.getText().length());
        search.setTextColor(neonHome() ? CheckMercadoNeonUi.TEXT : primaryText());
        search.setHintTextColor(neonHome() ? CheckMercadoNeonUi.MUTED : mutedText());
        search.setTextSize(15);
        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        search.setBackgroundColor(Color.TRANSPARENT);
        search.setPadding(0, 0, 0, 0);
        box.addView(search, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        TextView clear = new TextView(this);
        clear.setText("x");
        clear.setGravity(Gravity.CENTER);
        clear.setTextSize(16);
        clear.setTypeface(Typeface.DEFAULT_BOLD);
        clear.setTextColor(neonHome() ? CheckMercadoNeonUi.MUTED : mutedText());
        box.addView(clear, new LinearLayout.LayoutParams(dp(38), ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_search);
        icon.setColorFilter(neonHome() ? CheckMercadoNeonUi.GREEN : mutedText());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(22), dp(22));
        iconParams.setMargins(0, 0, dp(8), 0);
        box.addView(icon, iconParams);

        clear.setAlpha(search.getText().length() == 0 ? 0.45f : 1.0f);
        clear.setOnClickListener(v -> {
            if (search.getText().length() > 0) search.setText("");
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String next = s == null ? "" : s.toString();
                clear.setAlpha(next.isEmpty() ? 0.45f : 1.0f);
                int token = ++searchToken;
                search.postDelayed(() -> {
                    if (token == searchToken) callback.onSearchChanged(next);
                }, 180);
            }
        });
        View divider = new View(this);
        divider.setBackgroundColor(isDarkTheme() ? Color.argb(82, 57, 229, 108) : Color.argb(110, 148, 163, 184));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(dp(1), dp(28));
        dividerParams.setMargins(dp(4), 0, dp(4), 0);
        box.addView(divider, dividerParams);

        ImageButton filter = new ImageButton(this);
        filter.setImageResource(R.drawable.ic_filter_sliders);
        filter.setColorFilter(isDarkTheme()
                ? (filterActiveForCurrentScreen() ? CheckMercadoNeonUi.GREEN : CheckMercadoNeonUi.MUTED)
                : accent());
        filter.setBackgroundColor(Color.TRANSPARENT);
        filter.setPadding(dp(9), dp(9), dp(9), dp(9));
        filter.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        filter.setOnClickListener(v -> showSearchFilter());
        box.addView(filter, new LinearLayout.LayoutParams(dp(42), ViewGroup.LayoutParams.MATCH_PARENT));

        root.addView(row, matchHeightWithTop(dp(52), dp(10)));
        if (value != null && !value.isEmpty()) search.requestFocus();
    }

    private boolean filterActiveForCurrentScreen() {
        if (selectedIndex >= 0) return listItemFilter != 0;
        if (homeTab == 0) return homeListFilter != 0;
        if (homeTab == 3) return historyListFilter != 0;
        if (homeTab == 1) return stockCategoryFilter != null && !stockCategoryFilter.trim().isEmpty();
        if (homeTab == 6) return stockHistoryCategoryFilter != null && !stockHistoryCategoryFilter.trim().isEmpty();
        return false;
    }

    private void showSearchFilter() {
        if (selectedIndex >= 0) {
            String[] options = {"Todos", "Pendentes", "Concluidos", "Com preco", "Sem preco"};
            dialog()
                    .setTitle("Filtrar itens")
                    .setSingleChoiceItems(options, listItemFilter, (dialog, which) -> {
                        listItemFilter = which;
                        dialog.dismiss();
                        showListScreen();
                    })
                    .show();
            return;
        }
        if (homeTab == 0) {
            String[] options = {"Todas", "Abertas", "Protegidas", "Com itens", "Vazias"};
            dialog()
                    .setTitle("Filtrar listas")
                    .setSingleChoiceItems(options, homeListFilter, (dialog, which) -> {
                        homeListFilter = which;
                        dialog.dismiss();
                        showHomeScreen();
                    })
                    .show();
            return;
        }
        if (homeTab == 3) {
            String[] options = {"Todas", "Com itens", "Vazias"};
            dialog()
                    .setTitle("Filtrar historico")
                    .setSingleChoiceItems(options, historyListFilter, (dialog, which) -> {
                        historyListFilter = which;
                        dialog.dismiss();
                        showHistoryScreen();
                    })
                    .show();
            return;
        }
        if (homeTab == 1) {
            showCategoryFilter(false);
            return;
        }
        if (homeTab == 6) showCategoryFilter(true);
    }

    private void showCategoryFilter(boolean history) {
        List<String> categories = new ArrayList<>();
        categories.add("Todas as categorias");
        List<StockEntry> source = history ? stockHistory : stock;
        for (StockEntry entry : source) {
            String category = categoryOf(entry);
            boolean exists = false;
            for (String current : categories) {
                if (current.equalsIgnoreCase(category)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) categories.add(category);
        }
        String currentFilter = history ? stockHistoryCategoryFilter : stockCategoryFilter;
        int checked = 0;
        for (int i = 1; i < categories.size(); i++) {
            if (categories.get(i).equalsIgnoreCase(currentFilter)) {
                checked = i;
                break;
            }
        }
        String[] options = categories.toArray(new String[0]);
        dialog()
                .setTitle("Filtrar categoria")
                .setSingleChoiceItems(options, checked, (dialog, which) -> {
                    String selected = which == 0 ? "" : options[which];
                    if (history) {
                        stockHistoryCategoryFilter = selected;
                        dialog.dismiss();
                        showStockHistoryWindow();
                    } else {
                        stockCategoryFilter = selected;
                        dialog.dismiss();
                        showStockWindow(false);
                    }
                })
                .show();
    }

    private boolean matchesListSearch(ShoppingList list, String query) {
        String key = normalize(query);
        if (key.isEmpty()) return true;
        if (normalize(list.name).contains(key)) return true;
        for (ShoppingItem item : list.items) {
            if (normalize(item.name).contains(key)) return true;
            if (normalize(item.note).contains(key)) return true;
        }
        return false;
    }

    private boolean matchesHomeListFilter(ShoppingList list) {
        if (homeListFilter == 1) return !list.locked && !list.archived;
        if (homeListFilter == 2) return list.locked;
        if (homeListFilter == 3) return !list.items.isEmpty();
        if (homeListFilter == 4) return list.items.isEmpty();
        return true;
    }

    private boolean matchesHistoryListFilter(ShoppingList list) {
        if (historyListFilter == 1) return !list.items.isEmpty();
        if (historyListFilter == 2) return list.items.isEmpty();
        return true;
    }

    private boolean matchesItemSearch(ShoppingItem item, String query) {
        String key = normalize(query);
        return key.isEmpty() || normalize(item.name).contains(key) || normalize(item.note).contains(key);
    }

    private boolean matchesItemFilter(ShoppingItem item) {
        if (listItemFilter == 1) return !item.checked;
        if (listItemFilter == 2) return item.checked;
        if (listItemFilter == 3) return item.price > 0;
        if (listItemFilter == 4) return item.price <= 0;
        return true;
    }

    private boolean matchesStockSearch(StockEntry entry, String query) {
        String key = normalize(query);
        return key.isEmpty() || normalize(entry.name).contains(key) || normalize(categoryOf(entry)).contains(key);
    }

    private boolean matchesStockCategoryFilter(StockEntry entry, boolean history) {
        String filter = history ? stockHistoryCategoryFilter : stockCategoryFilter;
        return filter == null || filter.trim().isEmpty() || categoryOf(entry).equalsIgnoreCase(filter.trim());
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
        setDecimalInput(priceInput);
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
        setDecimalInput(unitInput);
        unitInput.setBackground(round(inputBg(), dp(14), stroke(), 1));
        unitInput.setPadding(dp(12), 0, dp(12), 0);
        unitInput.setSelectAllOnFocus(true);
        unitInput.setOnClickListener(v -> unitInput.selectAll());
        unitInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) unitInput.selectAll();
        });
        setupProductSuggestions();

        Button add = button("+", accent(), isLightColor(accent()) ? Color.rgb(15, 23, 42) : Color.WHITE);
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
        setupProductSuggestions(itemInput, priceInput, unitInput);
    }

    private void setupProductSuggestions(AutoCompleteTextView productInput, EditText priceField, EditText unitField) {
        Map<String, ProductSuggestion> latest = productSuggestionMap();
        if (latest.isEmpty()) return;
        List<ProductSuggestion> suggestions = new ArrayList<>(latest.values());
        Collections.sort(suggestions, (a, b) -> Long.compare(b.updatedAt, a.updatedAt));
        ArrayAdapter<ProductSuggestion> adapter = new ArrayAdapter<ProductSuggestion>(this, android.R.layout.simple_dropdown_item_1line, suggestions) {
            private final List<ProductSuggestion> all = new ArrayList<>(suggestions);

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

            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        String query = normalize(constraint == null ? "" : constraint.toString());
                        List<ProductSuggestion> filtered = new ArrayList<>();
                        for (ProductSuggestion suggestion : all) {
                            if (query.isEmpty() || normalize(suggestion.name).contains(query)) filtered.add(suggestion);
                        }
                        FilterResults results = new FilterResults();
                        results.values = filtered;
                        results.count = filtered.size();
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        clear();
                        if (results.values instanceof List) {
                            for (Object value : (List<?>) results.values) add((ProductSuggestion) value);
                        }
                        notifyDataSetChanged();
                    }
                };
            }
        };
        productInput.setAdapter(adapter);
        productInput.setThreshold(1);
        productInput.setDropDownBackgroundDrawable(round(cardBg(), dp(12), stroke(), 1));
        productInput.setOnItemClickListener((parent, view, position, id) -> {
            ProductSuggestion suggestion = (ProductSuggestion) parent.getItemAtPosition(position);
            if (suggestion == null) return;
            productInput.setText(suggestion.name);
            productInput.setSelection(productInput.getText().length());
            if (suggestion.price > 0) priceField.setText(formatPriceInput(suggestion.price));
            unitField.setText(suggestion.unit == null || suggestion.unit.trim().isEmpty() ? "1" : suggestion.unit);
            productInput.dismissDropDown();
        });
    }

    private void setupListNameSuggestions(AutoCompleteTextView input) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (ShoppingList list : lists) {
            if (list.name != null && !list.name.trim().isEmpty()) unique.add(list.name.trim());
        }
        if (unique.isEmpty()) return;
        List<String> names = new ArrayList<>(unique);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, names) {
            private final List<String> all = new ArrayList<>(names);

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setBackgroundColor(cardBg());
                if (view instanceof TextView) {
                    TextView text = (TextView) view;
                    text.setTextColor(primaryText());
                    text.setTextSize(15);
                    text.setPadding(dp(12), dp(10), dp(12), dp(10));
                }
                return view;
            }

            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        String query = normalize(constraint == null ? "" : constraint.toString());
                        List<String> filtered = new ArrayList<>();
                        for (String name : all) {
                            if (query.isEmpty() || normalize(name).contains(query)) filtered.add(name);
                        }
                        FilterResults results = new FilterResults();
                        results.values = filtered;
                        results.count = filtered.size();
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        clear();
                        if (results.values instanceof List) {
                            for (Object value : (List<?>) results.values) add((String) value);
                        }
                        notifyDataSetChanged();
                    }
                };
            }
        };
        input.setAdapter(adapter);
        input.setThreshold(1);
        input.setOnItemClickListener((parent, view, position, id) -> {
            String value = (String) parent.getItemAtPosition(position);
            input.setText(value);
            input.setSelection(input.getText().length());
            input.dismissDropDown();
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
        boolean any = false;
        for (ShoppingItem item : current.items) {
            if (matchesItemSearch(item, listSearch) && matchesItemFilter(item)) {
                any = true;
                break;
            }
        }
        if (!any && listSearch != null && !listSearch.trim().isEmpty()) {
            root.addView(infoCard("Nada encontrado", "Nenhum item da lista corresponde a pesquisa."), matchWrapWithTop(dp(10)));
            return;
        }
        if (current.sortMode == SORT_CHECKED_TOP) {
            addItemsByCheckedState(true);
            addItemsByCheckedState(false);
        } else if (current.sortMode == SORT_KEEP_POSITION) {
            for (int i = 0; i < current.items.size(); i++) {
                if (matchesItemSearch(current.items.get(i), listSearch) && matchesItemFilter(current.items.get(i))) {
                    root.addView(itemRow(current.items.get(i), i), matchWrapWithTop(dp(8)));
                }
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
            if (item.checked == checked && matchesItemSearch(item, listSearch) && matchesItemFilter(item)) {
                root.addView(itemRow(item, i), matchWrapWithTop(dp(8)));
            }
        }
    }

    private View itemRow(ShoppingItem item, int index) {
        ShoppingList current = lists.get(selectedIndex);
        String qtyText = formatQtyWithAutoUnit(item);
        String priceText = item.price > 0
                ? qtyText + " x " + money.format(item.price) + " (" + money.format(item.price * quantityOf(item)) + ")"
                : qtyText + " x R$ --";
        boolean priceBelow = true;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(10), dp(12), dp(10));
        row.setClipChildren(false);
        row.setClipToPadding(false);
        row.setBackground(item.checked ? checkedItemBg() : glassCardBg(0));
        elevate(row, item.checked ? 2 : 5);
        row.setOnLongClickListener(v -> {
            if (current.locked) return true;
            showItemOptions(item, index);
            return true;
        });

        CheckBox box = new CheckBox(this);
        box.setChecked(item.checked);
        box.setEnabled(!current.locked);
        box.setAlpha(current.locked ? 0.55f : 1.0f);
        box.setScaleX(1.28f);
        box.setScaleY(1.28f);
        tintCheckBox(box);
        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (current.locked) return;
            if (isChecked) {
                item.checked = true;
                if (lists.get(selectedIndex).saveCheckedToStock) {
                    addToStock(item, quantityOf(item), autoUnitForQuantity(quantityOf(item)));
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
        FrameLayout checkHolder = new FrameLayout(this);
        checkHolder.setClipChildren(false);
        checkHolder.setClipToPadding(false);
        checkHolder.addView(box, new FrameLayout.LayoutParams(dp(58), dp(58), Gravity.CENTER));
        LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(dp(64), dp(64));
        checkParams.setMargins(0, 0, dp(4), 0);
        row.addView(checkHolder, checkParams);

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
            showItemOptions(item, index);
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
        price.setPadding(dp(10), dp(4), dp(10), dp(4));
        price.setBackground(softPillBg(item.checked ? disabledText() : accent()));
        price.setOnLongClickListener(v -> {
            if (current.locked) return true;
            showItemOptions(item, index);
            return true;
        });
        if (priceBelow) {
            itemText.addView(price, matchWrapWithTop(dp(3)));
            if (item.note != null && !item.note.trim().isEmpty()) {
                TextView note = label(item.note, 13, false, mutedText());
                note.setPadding(0, dp(3), 0, 0);
                itemText.addView(note, matchWrap());
            }
            row.addView(itemText, weighted());
        } else {
            row.addView(itemText, weighted());
            LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(dp(94), ViewGroup.LayoutParams.WRAP_CONTENT);
            priceParams.setMargins(dp(6), 0, dp(6), 0);
            row.addView(price, priceParams);
        }

        if (!current.locked) {
            LinearLayout itemActions = new LinearLayout(this);
            itemActions.setOrientation(LinearLayout.VERTICAL);
            itemActions.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(dp(44), ViewGroup.LayoutParams.WRAP_CONTENT);
            actionParams.setMargins(dp(7), 0, 0, 0);
            row.addView(itemActions, actionParams);

            ImageButton more = moreMenuButton(mutedText());
            more.setOnClickListener(v -> showItemOptions(item, index));
            itemActions.addView(more, new LinearLayout.LayoutParams(dp(44), dp(40)));

            ImageButton remove = plainIconButton(R.drawable.ic_trash, isDarkTheme() ? CheckMercadoNeonUi.DANGER : Color.rgb(153, 27, 27), dp(7));
            remove.setOnClickListener(v -> removeListItem(index));
            LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(dp(40), dp(38));
            removeParams.setMargins(0, dp(4), 0, 0);
            itemActions.addView(remove, removeParams);
        }
        return row;
    }

    private void showItemOptions(ShoppingItem item, int index) {
        if (selectedIndex < 0 || lists.get(selectedIndex).locked) return;
        dialog()
                .setTitle(item.name)
                .setItems(new String[]{"Editar item", "Remover"}, (dialog, which) -> {
                    if (which == 0) promptEditItem(item);
                    else removeListItem(index);
                })
                .show();
    }

    private void removeListItem(int index) {
        if (selectedIndex < 0 || selectedIndex >= lists.size()) return;
        ShoppingList list = lists.get(selectedIndex);
        if (list.locked || index < 0 || index >= list.items.size()) return;
        list.items.remove(index);
        save();
        showListScreen();
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

    private void promptAddItem() {
        if (selectedIndex < 0 || lists.get(selectedIndex).locked) return;
        LinearLayout form = dialogForm();
        AutoCompleteTextView name = dialogAutoCompleteInput("Produto", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        configureSelectAll(name);
        EditText price = dialogInput("Preco", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setDecimalInput(price);
        EditText unit = dialogInput("Un", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setDecimalInput(unit);
        unit.setText("1");
        configureSelectAll(unit);
        EditText note = dialogInput("Nota", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        note.setSingleLine(false);
        note.setMinLines(2);
        setupProductSuggestions(name, price, unit);
        form.addView(name, matchHeight(dp(54)));
        form.addView(price, matchWrapWithTop(dp(8)));
        form.addView(unit, matchWrapWithTop(dp(8)));
        form.addView(note, matchWrapWithTop(dp(8)));
        dialog()
                .setTitle("Adicionar item")
                .setView(form)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String text = name.getText().toString().trim();
                    if (text.isEmpty()) return;
                    String unitText = unit.getText().toString().trim();
                    if (unitText.isEmpty()) unitText = "1";
                    ShoppingItem item = new ShoppingItem(text, parsePrice(price.getText().toString()), unitText);
                    item.note = note.getText().toString().trim();
                    lists.get(selectedIndex).items.add(item);
                    save();
                    showListScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
        setDecimalInput(qty);
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
            SpendingRecord record = new SpendingRecord(item.name, quantityOf(item), autoUnitForQuantity(quantityOf(item)), item.price, addedAt);
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
        AutoCompleteTextView name = dialogAutoCompleteInput("Produto", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        name.setText(item.name);
        configureSelectAll(name);
        EditText price = dialogInput("Preco", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setDecimalInput(price);
        if (item.price > 0) price.setText(formatPriceInput(item.price));
        EditText unit = dialogInput("Un", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setDecimalInput(unit);
        unit.setText(item.unit == null || item.unit.isEmpty() ? "1" : item.unit);
        configureSelectAll(unit);
        EditText note = dialogInput("Nota", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        note.setSingleLine(false);
        note.setMinLines(2);
        note.setText(item.note == null ? "" : item.note);
        setupProductSuggestions(name, price, unit);
        form.addView(name, matchHeight(dp(54)));
        form.addView(price, matchWrapWithTop(dp(8)));
        form.addView(unit, matchWrapWithTop(dp(8)));
        form.addView(note, matchWrapWithTop(dp(8)));
        dialog()
                .setTitle("Editar item")
                .setView(form)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String newName = name.getText().toString().trim();
                    if (!newName.isEmpty()) item.name = newName;
                    item.price = parsePrice(price.getText().toString());
                    item.unit = unit.getText().toString().trim();
                    if (item.unit.isEmpty()) item.unit = "1";
                    item.note = note.getText().toString().trim();
                    item.updatedAt = System.currentTimeMillis();
                    if (item.checked && selectedIndex >= 0 && lists.get(selectedIndex).saveCheckedToStock) {
                        addToStock(item, quantityOf(item), autoUnitForQuantity(quantityOf(item)));
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
        setDecimalInput(input);
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
        if (hits.size() <= 1) {
            Toast.makeText(this, "Sem hist\u00f3rico para este produto.", Toast.LENGTH_SHORT).show();
            return;
        }
        homeTab = 5;
        buildRoot();

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        Button close = iconButton("X", softButtonBg(), primaryText());
        close.setTextSize(18);
        close.setOnClickListener(v -> showListScreen());
        toolbar.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
        root.addView(toolbar, matchWrap());

        TextView title = label("Compara\u00e7\u00e3o de pre\u00e7os", 22, true, primaryText());
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrapWithTop(dp(14)));

        TextView subtitle = label("Ordenado do maior para o menor pre\u00e7o unit\u00e1rio.", 14, false, mutedText());
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, matchWrapWithTop(dp(4)));

        for (PriceHit hit : hits) {
            root.addView(comparisonCard(hit, item.price), matchWrapWithTop(dp(10)));
        }

        setContentView(rootScroll());
    }

    private View comparisonCard(PriceHit hit, double sourcePrice) {
        int trendColor;
        int trendIcon;
        String trendText;
        if (sourcePrice > 0 && hit.price > sourcePrice + 0.000001) {
            trendColor = Color.rgb(220, 38, 38);
            trendIcon = R.drawable.ic_arrow_up;
            trendText = "Mais caro";
        } else if (sourcePrice > 0 && hit.price < sourcePrice - 0.000001) {
            trendColor = Color.rgb(22, 163, 74);
            trendIcon = R.drawable.ic_arrow_down;
            trendText = "Mais barato";
        } else {
            trendColor = Color.rgb(234, 88, 12);
            trendIcon = R.drawable.ic_minus;
            trendText = "Mesmo pre\u00e7o";
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(round(cardBg(), dp(18), blend(trendColor, stroke(), 0.45f), 1));
        elevate(card, 3);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top, matchWrap());

        ImageView icon = new ImageView(this);
        icon.setImageResource(trendIcon);
        icon.setColorFilter(trendColor);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        iconParams.setMargins(0, 0, dp(10), 0);
        top.addView(icon, iconParams);

        TextView trend = label(trendText, 15, true, trendColor);
        top.addView(trend, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView price = label(money.format(hit.price), 16, true, trendColor);
        price.setGravity(Gravity.END);
        price.setPadding(dp(10), dp(5), dp(10), dp(5));
        price.setBackground(softPillBg(trendColor));
        top.addView(price);

        card.addView(comparisonDataRow(R.drawable.ic_clipboard_list, "Lista", hit.listName, primaryText()), matchWrapWithTop(dp(10)));
        card.addView(comparisonDataRow(R.drawable.ic_cart, "Produto", hit.itemName, primaryText()), matchWrapWithTop(dp(8)));
        card.addView(comparisonDataRow(R.drawable.ic_money_circle, "Pre\u00e7o unit\u00e1rio", money.format(hit.price), trendColor), matchWrapWithTop(dp(8)));
        card.addView(comparisonDataRow(R.drawable.ic_calendar_tiny, "Data da compra", formatDateLabel(hit.purchaseAt), mutedText()), matchWrapWithTop(dp(8)));
        return card;
    }

    private View comparisonDataRow(int iconRes, String title, String value, int valueColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(round(inputBg(), dp(14), softDividerColor(), 1));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(mutedText());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(18), dp(18));
        iconParams.setMargins(0, 0, dp(10), 0);
        row.addView(icon, iconParams);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        texts.addView(label(title, 12, false, mutedText()));
        TextView content = label(isBlank(value) ? "-" : value, 15, true, valueColor);
        content.setPadding(0, dp(2), 0, 0);
        texts.addView(content);
        return row;
    }

    private boolean hasComparablePrices(ShoppingItem item) {
        return findComparablePrices(item).size() > 1;
    }

    private List<PriceHit> findComparablePrices(ShoppingItem item) {
        String target = normalize(item.name);
        List<PriceHit> hits = new ArrayList<>();
        for (ShoppingList list : lists) {
            for (ShoppingItem other : list.items) {
                if (other.price <= 0) continue;
                if (!normalize(other.name).equals(target)) continue;
                long purchaseAt = list.createdAt > 0 ? list.createdAt : other.updatedAt;
                hits.add(new PriceHit(list.name, other.name, other.price, other.updatedAt, purchaseAt));
            }
        }
        Collections.sort(hits, (a, b) -> Double.compare(b.price, a.price));
        return hits;
    }

    private void promptNewList() {
        LinearLayout form = dialogForm();
        AutoCompleteTextView input = dialogAutoCompleteInput("Nome da lista", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        setupListNameSuggestions(input);
        form.addView(input, matchHeight(dp(54)));
        EditText budget = dialogInput("Or\u00e7amento", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setDecimalInput(budget);
        form.addView(budget, matchHeightWithTop(dp(54), dp(8)));
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
                    list.color = randomListColor();
                    list.budget = parsePrice(budget.getText().toString());
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
                ? new String[]{"Copiar", "Remover"}
                : new String[]{"Concluir esta lista", "Editar nome", "Mudar cor", "Remover"};
        dialog()
                .setTitle(list.name)
                .setItems(options, (dialog, which) -> {
                    if (list.locked && which == 0) {
                        copyListFromHistory(index);
                    } else if (list.locked || which == 3) {
                        confirmDeleteList(index);
                    } else if (which == 0) {
                        completeList(index);
                    } else if (which == 1) {
                        promptEditList(index);
                    } else if (which == 2) {
                        promptListColor(index);
                    }
                })
                .show();
    }

    private void completeList(int index) {
        if (index < 0 || index >= lists.size()) return;
        ShoppingList list = lists.get(index);
        if (list.locked) return;
        int changed = 0;
        for (ShoppingItem item : list.items) {
            if (item.checked) continue;
            item.checked = true;
            changed++;
            if (list.saveCheckedToStock) {
                double quantity = quantityOf(item);
                addToStock(item, quantity, autoUnitForQuantity(quantity));
            }
        }
        if (changed > 0) {
            save();
            Toast.makeText(this, changed + " item(ns) concluido(s).", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lista ja estava concluida.", Toast.LENGTH_SHORT).show();
        }
        showHomeScreen();
    }

    private void promptEditList() {
        if (selectedIndex < 0) return;
        promptEditList(selectedIndex);
    }

    private void promptEditList(int index) {
        ShoppingList list = lists.get(index);
        if (list.locked) return;
        LinearLayout form = dialogForm();
        EditText input = dialogInput("Nome da lista", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(list.name);
        input.setSelection(input.getText().length());
        form.addView(input, matchHeight(dp(54)));
        EditText budget = dialogInput("Or\u00e7amento", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setDecimalInput(budget);
        if (list.budget > 0) {
            budget.setText(formatPriceInput(list.budget));
            budget.setSelection(budget.getText().length());
        }
        form.addView(budget, matchHeightWithTop(dp(54), dp(8)));
        dialog()
                .setTitle("Editar lista")
                .setView(form)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) list.name = name;
                    list.budget = parsePrice(budget.getText().toString());
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

    private void copyListFromHistory(int index) {
        try {
            ShoppingList copy = ShoppingList.fromJson(lists.get(index).toJson());
            copy.id = UUID.randomUUID().toString();
            copy.name = copy.name + " (copia)";
            copy.locked = false;
            copy.archived = false;
            copy.lockedAt = 0;
            copy.createdAt = System.currentTimeMillis();
            for (ShoppingItem item : copy.items) {
                item.id = UUID.randomUUID().toString();
                item.checked = false;
                item.stockId = "";
            }
            lists.add(0, copy);
            selectedIndex = 0;
            selectedFromHistory = false;
            save();
            showListScreen();
        } catch (JSONException e) {
            Toast.makeText(this, "Nao foi possivel copiar esta lista.", Toast.LENGTH_SHORT).show();
        }
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

    private void showCredits() {
        LinearLayout form = dialogForm();
        TextView title = label("Cr\u00e9ditos", 22, true, primaryText());
        title.setGravity(Gravity.CENTER);
        form.addView(title, matchWrap());

        TextView author = label("Armando Neto", 17, true, accent());
        author.setGravity(Gravity.CENTER);
        author.setPadding(0, dp(12), 0, 0);
        form.addView(author, matchWrap());

        TextView codex = label("ChatGPT (Codex) 5.5", 15, false, primaryText());
        codex.setGravity(Gravity.CENTER);
        codex.setPadding(0, dp(6), 0, 0);
        form.addView(codex, matchWrap());

        TextView testers = label("Agradecimento aos testers:\nArmando Junior e Gabriel Lima", 15, false, mutedText());
        testers.setGravity(Gravity.CENTER);
        testers.setPadding(0, dp(14), 0, 0);
        form.addView(testers, matchWrap());

        final AlertDialog[] credits = new AlertDialog[1];
        author.setOnClickListener(v -> registerCreditsSecret(0, credits[0]));
        codex.setOnClickListener(v -> registerCreditsSecret(1, credits[0]));
        testers.setOnClickListener(v -> registerCreditsSecret(2, credits[0]));

        credits[0] = dialog()
                .setView(form)
                .setPositiveButton("Fechar", null)
                .show();
    }

    private void registerCreditsSecret(int step, AlertDialog dialog) {
        if (step == creditsSecretStep) {
            creditsSecretStep++;
        } else {
            creditsSecretStep = step == 0 ? 1 : 0;
        }
        if (creditsSecretStep >= 3) {
            creditsSecretStep = 0;
            if (dialog != null) dialog.dismiss();
            Toast.makeText(this, "Compra Invaders desbloqueado!", Toast.LENGTH_SHORT).show();
            showCompraInvaders();
        }
    }

    private void confirmDeleteList(int index) {
        dialog()
                .setTitle("Remover lista?")
                .setMessage(lists.get(index).name)
                .setPositiveButton("Remover", (dialog, which) -> {
                    ShoppingList list = lists.get(index);
                    if (list.archived || list.locked) {
                        list.deletedFromHistory = true;
                        addSpendingRecordsForList(list);
                    } else {
                        lists.remove(index);
                    }
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
                    rememberStockUndo();
                    stock.remove(entry);
                    saveStock();
                    showStockWindow(false);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showStockOptions(StockEntry entry) {
        boolean selected = entry != null && selectedStockIds.contains(entry.id) && !selectedStockIds.isEmpty();
        String[] options = selected
                ? new String[]{"Editar quantidade", "Editar categoria", "Dar baixa", "Dar baixa em todos"}
                : new String[]{"Editar quantidade", "Editar categoria", "Dar baixa"};
        dialog()
                .setTitle(entry.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        promptEditStockQuantity(entry);
                    } else if (which == 1) {
                        promptEditStockCategory(entry);
                    } else if (which == 2) {
                        confirmCheckoutStock(entry);
                    } else {
                        confirmCheckoutSelectedStock();
                    }
                })
                .show();
    }

    private void confirmCheckoutStock(StockEntry entry) {
        dialog()
                .setTitle("Dar baixa no estoque?")
                .setMessage(entry.name + "\nO item saira do estoque atual e ficara no historico.")
                .setPositiveButton("Dar baixa", (dialog, which) -> checkoutStock(entry))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void checkoutStock(StockEntry entry) {
        rememberStockUndo();
        stock.remove(entry);
        entry.consumedAt = System.currentTimeMillis();
        entry.updatedAt = entry.consumedAt;
        stockHistory.add(0, entry);
        saveStock();
        saveStockHistory();
        markStockHistoryPending();
        showStockWindow(false);
    }

    private void confirmCheckoutSelectedStock() {
        List<StockEntry> rows = selectedStockEntries();
        if (rows.isEmpty()) return;
        dialog()
                .setTitle("Dar baixa em todos?")
                .setMessage(rows.size() + " item(ns) selecionado(s) sair\u00e3o do estoque atual e ficar\u00e3o no hist\u00f3rico.")
                .setPositiveButton("Dar baixa", (dialog, which) -> checkoutSelectedStock(rows))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void checkoutSelectedStock(List<StockEntry> rows) {
        if (rows == null || rows.isEmpty()) return;
        rememberStockUndo();
        long now = System.currentTimeMillis();
        for (int i = rows.size() - 1; i >= 0; i--) {
            StockEntry entry = rows.get(i);
            if (!stock.remove(entry)) continue;
            entry.consumedAt = now;
            entry.updatedAt = now;
            stockHistory.add(0, entry);
        }
        selectedStockIds.clear();
        saveStock();
        saveStockHistory();
        markStockHistoryPending();
        showStockWindow(false);
    }

    private void markStockHistoryPending() {
        stockHistoryPending = true;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_STOCK_HISTORY_PENDING, true).apply();
    }

    private void clearStockHistoryPending() {
        if (!stockHistoryPending) return;
        stockHistoryPending = false;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_STOCK_HISTORY_PENDING, false).apply();
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
                    rememberStockUndo();
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
        rememberStockUndo();
        String clean = category == null ? "" : category.trim();
        entry.category = clean.isEmpty() ? "Outros" : clean;
        entry.updatedAt = System.currentTimeMillis();
        saveStock();
        updateSpendingCategoryForStock(entry);
        showStockWindow(false);
    }

    private void rememberStockUndo() {
        stockUndoStockJson = stockJson(stock);
        stockUndoHistoryJson = stockJson(stockHistory);
    }

    private String stockJson(List<StockEntry> entries) {
        JSONArray array = new JSONArray();
        for (StockEntry entry : entries) {
            try {
                array.put(entry.toJson());
            } catch (JSONException ignored) {
            }
        }
        return array.toString();
    }

    private void undoStockAction() {
        if (stockUndoStockJson == null || stockUndoHistoryJson == null) return;
        stock.clear();
        stockHistory.clear();
        try {
            JSONArray stockArray = new JSONArray(stockUndoStockJson);
            for (int i = 0; i < stockArray.length(); i++) stock.add(StockEntry.fromJson(stockArray.getJSONObject(i)));
            JSONArray historyArray = new JSONArray(stockUndoHistoryJson);
            for (int i = 0; i < historyArray.length(); i++) stockHistory.add(StockEntry.fromJson(historyArray.getJSONObject(i)));
            saveStock();
            saveStockHistory();
            stockUndoStockJson = null;
            stockUndoHistoryJson = null;
            Toast.makeText(this, "Acao desfeita.", Toast.LENGTH_SHORT).show();
            if (homeTab == 6) showStockHistoryWindow(); else showStockWindow(false);
        } catch (JSONException e) {
            Toast.makeText(this, "Nao foi possivel desfazer.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareSelectedList() {
        CharSequence[] options = {"Lista completa", "Com privacidade"};
        dialog()
                .setTitle("Compartilhar")
                .setItems(options, (dialog, which) -> shareSelectedList(which == 1))
                .show();
    }

    private void shareSelectedList(boolean privacyMode) {
        try {
            String link = buildShareLink(lists.get(selectedIndex), privacyMode);
            shareListUrl(link);
        } catch (Exception e) {
            Toast.makeText(this, "Nao foi possivel compartilhar esta lista.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareListUrl(String link) {
        Toast.makeText(this, "Preparando link...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            String finalLink = link;
            try {
                finalLink = shortenPublicUrl(link);
            } catch (Exception ignored) {
            }
            String shareText = finalLink;
            runOnUiThread(() -> {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(send, "Compartilhar lista"));

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Check Mercado", shareText));
                }
            });
        }).start();
    }

    private String shortenPublicUrl(String url) throws Exception {
        if (!isPublicHttpUrl(url)) return url;
        HttpURLConnection connection = (HttpURLConnection) new URL(SHORTENER_ENDPOINT).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        JSONObject body = new JSONObject();
        body.put("url", url);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int code = connection.getResponseCode();
        String response = readText(code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream());
        connection.disconnect();
        if (code < 200 || code >= 300 || isBlank(response)) return url;
        JSONObject json = new JSONObject(response);
        String shortUrl = json.optString("shortUrl", "");
        return json.optBoolean("ok", false) && isPublicHttpUrl(shortUrl) ? shortUrl : url;
    }

    private boolean isPublicHttpUrl(String value) {
        if (isBlank(value)) return false;
        Uri uri = Uri.parse(value);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) return false;
        if (isBlank(host)) return false;
        String key = host.toLowerCase(Locale.ROOT);
        return !key.equals("localhost")
                && !key.equals("127.0.0.1")
                && !key.equals("0.0.0.0")
                && !key.startsWith("10.")
                && !key.startsWith("192.168.")
                && !key.matches("172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
    }

    private String buildShareLink(ShoppingList list, boolean privacyMode) throws Exception {
        String payload = encodeCompressed((privacyMode ? privateShareJson(list) : list.toJson()).toString());
        return SHARE_BASE + payload;
    }

    private JSONObject privateShareJson(ShoppingList list) throws JSONException {
        JSONObject json = list.toJson();
        JSONArray items = new JSONArray();
        for (ShoppingItem source : list.items) {
            ShoppingItem item = new ShoppingItem(source.name, source.price, "1");
            item.id = source.id;
            item.checked = false;
            item.note = "";
            item.updatedAt = source.updatedAt;
            item.stockId = "";
            items.put(item.toJson());
        }
        json.put("items", items);
        json.put("saveCheckedToStock", false);
        json.put("locked", false);
        json.put("archived", false);
        json.put("deletedFromHistory", false);
        return json;
    }

    private void handleIncomingIntent(Intent intent) {
        if (pendingIntentHandled && intent == getIntent()) return;
        if (intent == null) return;
        Uri data = intent.getData();
        if (data != null) {
            String backup = extractBackup(data);
            if (backup != null) {
                promptImportBackup(backup, false);
                return;
            }
            String payload = extractPayload(data);
            if (payload != null) importPayload(payload);
        }
    }

    private String extractBackup(Uri data) {
        if (data == null) return null;
        if (("http".equals(data.getScheme()) || "https".equals(data.getScheme()))
                && PAGES_HOST.equals(data.getHost())
                && data.getPath() != null
                && data.getPath().startsWith(PAGES_PATH)) {
            return data.getQueryParameter("backup");
        }
        return null;
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

    private String extractBackup(String text) {
        if (text == null) return null;
        String value = text.trim();
        int start = value.indexOf("http");
        if (start > 0) value = value.substring(start);
        try {
            String backup = extractBackup(Uri.parse(value));
            if (backup != null && !backup.trim().isEmpty()) return backup;
        } catch (Exception ignored) {
        }
        int marker = value.indexOf("backup=");
        if (marker >= 0) return value.substring(marker + "backup=".length());
        if (value.startsWith("CompraLinkBackup:")) return value.substring("CompraLinkBackup:".length());
        return null;
    }

    private void importClipboardListIfPresent() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip() || clipboard.getPrimaryClip() == null) return;
        ClipData clip = clipboard.getPrimaryClip();
        if (clip.getItemCount() == 0) return;
        CharSequence text = clip.getItemAt(0).coerceToText(this);
        String backup = extractBackup(text == null ? null : text.toString());
        if (backup != null && !backup.trim().isEmpty()) {
            String cleanBackup = cleanPayload(backup);
            String last = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_LAST_CLIPBOARD_PAYLOAD, "");
            if (!cleanBackup.equals(last)) {
                promptImportBackup(cleanBackup, true);
            }
            return;
        }
        String payload = extractPayload(text == null ? null : text.toString());
        if (payload == null || payload.trim().isEmpty()) return;
        String clean = cleanPayload(payload);
        String last = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_LAST_CLIPBOARD_PAYLOAD, "");
        if (clean.equals(last)) return;
        if (importPayload(clean)) {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_LAST_CLIPBOARD_PAYLOAD, clean).apply();
            Toast.makeText(this, "Lista importada da \u00e1rea de transfer\u00eancia.", Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "Link de lista inv\u00e1lido.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    private boolean saveImportedList(ShoppingList imported) {
        if (imported == null) return false;
        String importedName = imported.name == null ? "" : imported.name.trim();
        if (imported.items.isEmpty()
                && (isBlank(importedName) || "Lista".equalsIgnoreCase(importedName) || "Nova lista".equalsIgnoreCase(importedName))) {
            Toast.makeText(this, "Lista compartilhada vazia ignorada.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (imported.id == null || imported.id.trim().isEmpty()) {
            imported.id = UUID.randomUUID().toString();
        }
        int existingIndex = findListIndexById(imported.id);
        if (existingIndex >= 0) {
            ShoppingList existing = lists.get(existingIndex);
            selectedIndex = existingIndex;
            if (listsEquivalent(existing, imported)) {
                Toast.makeText(this, "Lista compartilhada ja existe e esta igual.", Toast.LENGTH_SHORT).show();
                showListScreen();
                return true;
            }
            String summary = listChangeSummary(existing, imported);
            dialog()
                    .setTitle("Atualizar lista?")
                    .setMessage(summary)
                    .setPositiveButton("Atualizar", (dialog, which) -> {
                        preserveLocalStockLinks(existing, imported);
                        lists.set(existingIndex, imported);
                        selectedIndex = -1;
                        save();
                        flashImportedListId = imported.id;
                        showHomeScreen();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        }
        clearImportedStockLinks(imported);
        lists.add(0, imported);
        selectedIndex = -1;
        save();
        flashImportedListId = imported.id;
        Toast.makeText(this, "Lista importada.", Toast.LENGTH_SHORT).show();
        showHomeScreen();
        return true;
    }

    private boolean listsEquivalent(ShoppingList a, ShoppingList b) {
        return listChangeSummary(a, b).startsWith("A lista compartilhada esta igual");
    }

    private String listChangeSummary(ShoppingList current, ShoppingList incoming) {
        int added = 0;
        int removed = 0;
        int changed = 0;
        Map<String, ShoppingItem> currentItems = itemCompareMap(current);
        Map<String, ShoppingItem> incomingItems = itemCompareMap(incoming);
        for (String key : incomingItems.keySet()) {
            ShoppingItem incomingItem = incomingItems.get(key);
            ShoppingItem currentItem = currentItems.get(key);
            if (currentItem == null) {
                added++;
            } else if (itemChanged(currentItem, incomingItem)) {
                changed++;
            }
        }
        for (String key : currentItems.keySet()) {
            if (!incomingItems.containsKey(key)) removed++;
        }
        boolean budgetChanged = Math.abs(current.budget - incoming.budget) > 0.009;
        if (added == 0 && removed == 0 && changed == 0 && stringEquals(current.name, incoming.name) && !budgetChanged) {
            return "A lista compartilhada esta igual a lista salva.";
        }
        StringBuilder message = new StringBuilder("Esta lista compartilhada ja existe, mas ha alteracoes.");
        if (!stringEquals(current.name, incoming.name)) message.append("\nNome atualizado.");
        if (budgetChanged) message.append("\nOr\u00e7amento atualizado.");
        if (added > 0) message.append("\n").append(added).append(added == 1 ? " item novo." : " itens novos.");
        if (removed > 0) message.append("\n").append(removed).append(removed == 1 ? " item removido." : " itens removidos.");
        if (changed > 0) message.append("\n").append(changed).append(changed == 1 ? " item com valor, unidade, nota ou status atualizado." : " itens com valor, unidade, nota ou status atualizados.");
        message.append("\n\nDeseja atualizar a lista atual?");
        return message.toString();
    }

    private Map<String, ShoppingItem> itemCompareMap(ShoppingList list) {
        Map<String, ShoppingItem> map = new LinkedHashMap<>();
        for (ShoppingItem item : list.items) {
            String key = item.id == null || item.id.isEmpty() ? normalize(item.name) : item.id;
            map.put(key, item);
        }
        return map;
    }

    private boolean itemChanged(ShoppingItem a, ShoppingItem b) {
        return !stringEquals(a.name, b.name)
                || a.checked != b.checked
                || Math.abs(a.price - b.price) > 0.000001
                || !stringEquals(a.unit, b.unit)
                || !stringEquals(a.note, b.note);
    }

    private boolean stringEquals(String a, String b) {
        return (a == null ? "" : a).equals(b == null ? "" : b);
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

    private String buildEncryptedBackupFileText() throws Exception {
        String backupLink = BACKUP_BASE + encodeCompressed(buildBackupJson().toString());
        return BACKUP_FILE_PREFIX + encryptBackupText(backupLink);
    }

    private String encryptBackupText(String text) throws Exception {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, backupKey(), new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        JSONObject json = new JSONObject();
        json.put("v", 2);
        json.put("iv", Base64.encodeToString(iv, Base64.NO_WRAP));
        json.put("data", Base64.encodeToString(encrypted, Base64.NO_WRAP));
        return json.toString();
    }

    private String decryptBackupFileText(String text) throws Exception {
        String clean = text == null ? "" : text.trim();
        if (!clean.startsWith(BACKUP_FILE_PREFIX)) {
            if (extractBackup(clean) != null || clean.startsWith("CompraLinkBackup:")) return clean;
            throw new JSONException("Formato de backup desconhecido");
        }
        JSONObject json = new JSONObject(clean.substring(BACKUP_FILE_PREFIX.length()));
        byte[] iv = Base64.decode(json.getString("iv"), Base64.NO_WRAP);
        byte[] encrypted = Base64.decode(json.getString("data"), Base64.NO_WRAP);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, backupKey(), new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    private SecretKeySpec backupKey() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest("Check Mercado backup file key v2 MBZerker".getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }

    private String backupFileName() {
        Calendar now = Calendar.getInstance();
        return String.format(Locale.US, "CheckMercado-backup-%04d%02d%02d-%02d%02d.cmbackup",
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH),
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE));
    }

    private String readText(InputStream input) throws Exception {
        if (input == null) return "";
        try (InputStream in = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) output.write(buffer, 0, read);
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
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

    private void promptImportBackup(String rawBackup, boolean fromClipboard) {
        String clean = cleanPayload(rawBackup);
        String message = fromClipboard
                ? "Ha um backup do Check Mercado copiado na area de transferencia. Deseja importar agora?"
                : "Este link contem um backup do Check Mercado. Deseja importar agora?";
        dialog()
                .setTitle("Importar backup?")
                .setMessage(message + "\n\nOs dados do backup serao adicionados/atualizados no app.")
                .setPositiveButton("Importar", (dialog, which) -> {
                    if (importBackupPayload(clean)) {
                        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_LAST_CLIPBOARD_PAYLOAD, clean).apply();
                        Toast.makeText(this, "Backup importado.", Toast.LENGTH_SHORT).show();
                        showHomeScreen();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void promptImportBackupFile(String cleanBackup) {
        dialog()
                .setTitle("Restaurar backup?")
                .setMessage("Este arquivo contem um backup criptografado do Check Mercado.\n\nOs dados serao adicionados/atualizados no app.")
                .setPositiveButton("Restaurar", (dialog, which) -> {
                    if (importBackupPayload(cleanBackup)) {
                        Toast.makeText(this, "Backup restaurado.", Toast.LENGTH_SHORT).show();
                        showHomeScreen();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean importBackupPayload(String rawBackup) {
        try {
            JSONObject backup = new JSONObject(decodeCompressed(cleanPayload(rawBackup)));
            importBackupJson(backup);
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Backup invalido.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void importBackupJson(JSONObject backup) throws JSONException {
        JSONArray listArray = backup.optJSONArray("lists");
        if (listArray != null) {
            for (int i = 0; i < listArray.length(); i++) {
                ShoppingList imported = ShoppingList.fromJson(listArray.getJSONObject(i));
                int existing = findListIndexById(imported.id);
                if (existing >= 0) lists.set(existing, imported);
                else lists.add(0, imported);
            }
            save();
        }

        JSONArray stockArray = backup.optJSONArray("stock");
        if (stockArray != null) {
            stock.clear();
            for (int i = 0; i < stockArray.length(); i++) stock.add(StockEntry.fromJson(stockArray.getJSONObject(i)));
            saveStock();
        }

        JSONArray stockHistoryArray = backup.optJSONArray("stockHistory");
        if (stockHistoryArray != null) {
            stockHistory.clear();
            for (int i = 0; i < stockHistoryArray.length(); i++) stockHistory.add(StockEntry.fromJson(stockHistoryArray.getJSONObject(i)));
            saveStockHistory();
        }

        JSONArray spendingArray = backup.optJSONArray("spendingHistory");
        if (spendingArray != null) {
            spendingHistory.clear();
            for (int i = 0; i < spendingArray.length(); i++) spendingHistory.add(SpendingRecord.fromJson(spendingArray.getJSONObject(i)));
            saveSpendingHistory();
        }
        JSONArray monthlyBudgetArray = backup.optJSONArray("monthlyBudgetEntries");
        if (monthlyBudgetArray != null) {
            monthlyBudgetEntries.clear();
            for (int i = 0; i < monthlyBudgetArray.length(); i++) monthlyBudgetEntries.add(MonthlyBudgetEntry.fromJson(monthlyBudgetArray.getJSONObject(i)));
            saveMonthlyBudget();
        }

        JSONArray monthlyIncomeArray = backup.optJSONArray("monthlyBudgetIncomes");
        if (monthlyIncomeArray != null) {
            monthlyBudgetIncomes.clear();
            for (int i = 0; i < monthlyIncomeArray.length(); i++) monthlyBudgetIncomes.add(MonthlyBudgetIncome.fromJson(monthlyIncomeArray.getJSONObject(i)));
            saveMonthlyBudgetIncomes();
        }


        JSONObject settings = backup.optJSONObject("settings");
        if (settings != null) {
            themeMode = settings.optInt("themeMode", themeMode);
            accentColor = settings.optInt("accentColor", accentColor);
            spendingRangeMonths = settings.optInt("spendingRangeMonths", spendingRangeMonths);
            monthlyGoal = settings.optDouble("monthlyGoal", monthlyGoal);
            monthlyBudgetLimit = settings.optDouble("monthlyBudgetLimit", monthlyBudgetLimit);
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putInt(KEY_THEME, themeMode)
                    .putInt(KEY_ACCENT, accentColor)
                    .putInt(KEY_SPENDING_RANGE, spendingRangeMonths)
                    .putLong(KEY_MONTHLY_GOAL, Double.doubleToLongBits(monthlyGoal))
                    .putLong(KEY_MONTHLY_BUDGET_LIMIT, Double.doubleToLongBits(monthlyBudgetLimit))
                    .apply();
        }
    }

    private void loadStockHistory() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_STOCK_HISTORY, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            stockHistory.clear();
            for (int i = 0; i < array.length(); i++) {
                stockHistory.add(StockEntry.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            stockHistory.clear();
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

    private void loadMonthlyBudget() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_MONTHLY_BUDGET_ENTRIES, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            monthlyBudgetEntries.clear();
            for (int i = 0; i < array.length(); i++) {
                monthlyBudgetEntries.add(MonthlyBudgetEntry.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            monthlyBudgetEntries.clear();
        }
    }

    private void loadMonthlyBudgetIncomes() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_MONTHLY_BUDGET_INCOMES, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            monthlyBudgetIncomes.clear();
            for (int i = 0; i < array.length(); i++) {
                monthlyBudgetIncomes.add(MonthlyBudgetIncome.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            monthlyBudgetIncomes.clear();
        }
    }
    private void saveMonthlyBudget() {
        JSONArray array = new JSONArray();
        for (MonthlyBudgetEntry entry : monthlyBudgetEntries) {
            try {
                array.put(entry.toJson());
            } catch (JSONException ignored) {
            }
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(KEY_MONTHLY_BUDGET_ENTRIES, array.toString())
                .putLong(KEY_MONTHLY_BUDGET_LIMIT, Double.doubleToLongBits(monthlyBudgetLimit))
                .apply();
    }
    private void saveMonthlyBudgetIncomes() {
        JSONArray array = new JSONArray();
        for (MonthlyBudgetIncome income : monthlyBudgetIncomes) {
            try {
                array.put(income.toJson());
            } catch (JSONException ignored) {
            }
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(KEY_MONTHLY_BUDGET_INCOMES, array.toString())
                .apply();
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

    private void saveStockHistory() {
        JSONArray array = new JSONArray();
        for (StockEntry entry : stockHistory) {
            try {
                array.put(entry.toJson());
            } catch (JSONException ignored) {
            }
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_STOCK_HISTORY, array.toString()).apply();
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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

    private AutoCompleteTextView dialogAutoCompleteInput(String hint, int inputType) {
        AutoCompleteTextView input = new AutoCompleteTextView(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setTextColor(primaryText());
        input.setHintTextColor(mutedText());
        input.setLinkTextColor(accent());
        input.setBackground(round(inputBg(), dp(12), stroke(), 1));
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setInputType(inputType);
        input.setDropDownBackgroundDrawable(round(cardBg(), dp(12), stroke(), 1));
        return input;
    }

    private void configureSelectAll(EditText input) {
        input.setSelectAllOnFocus(true);
        input.setOnClickListener(v -> input.selectAll());
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) input.selectAll();
        });
    }

    private void setDecimalInput(EditText input) {
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setKeyListener(DigitsKeyListener.getInstance("0123456789,."));
    }

    private StyledDialogBuilder dialog() {
        return new StyledDialogBuilder(this);
    }

    private void styleDialog(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(round(cardBg(), dp(22), stroke(), 1));
            window.setDimAmount(isDarkTheme() ? 0.72f : 0.42f);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
            GradientDrawable divider = new GradientDrawable();
            divider.setColor(softDividerColor());
            list.setDivider(divider);
            list.setDividerHeight(dp(1));
            list.setPadding(dp(6), dp(6), dp(6), dp(6));
        }
        int titleId = getResources().getIdentifier("alertTitle", "id", "android");
        TextView title = titleId == 0 ? null : dialog.findViewById(titleId);
        if (title != null) {
            title.setTextColor(isDarkTheme() ? CheckMercadoNeonUi.TEXT : primaryText());
            title.setTextSize(20);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setPadding(title.getPaddingLeft(), title.getPaddingTop(), title.getPaddingRight(), dp(12));
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

    private LinearLayout iconText(int iconRes, String text, int size, boolean bold, int textColor, int iconColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(iconColor);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(16), dp(16));
        iconParams.setMargins(0, 0, dp(6), 0);
        row.addView(icon, iconParams);

        TextView label = label(text, size, bold, textColor);
        row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private String formatQty(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private String formatQtyWithAutoUnit(ShoppingItem item) {
        double quantity = quantityOf(item);
        return formatQty(quantity) + " " + autoUnitForQuantity(quantity);
    }

    private String formatStockQuantity(StockEntry entry) {
        String unit = entry.unit == null || entry.unit.trim().isEmpty()
                ? autoUnitForQuantity(entry.quantity)
                : entry.unit.trim();
        return formatQty(entry.quantity) + " " + unit;
    }

    private String autoUnitForQuantity(double quantity) {
        return isFractional(quantity) ? "kg" : "un";
    }

    private boolean isFractional(double value) {
        return Math.abs(value - Math.rint(value)) > 0.000001;
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

    private double stockDurationDays(StockEntry entry) {
        long end = entry.consumedAt > 0 ? entry.consumedAt : System.currentTimeMillis();
        long diff = Math.max(0, end - entry.addedAt);
        return diff / 86400000.0;
    }

    private String formatDurationDays(double days) {
        if (days < 1) {
            int hours = Math.max(1, (int) Math.round(days * 24));
            return hours + (hours == 1 ? " hora" : " horas");
        }
        if (days < 10) {
            return String.format(new Locale("pt", "BR"), "%.1f dias", days);
        }
        long rounded = Math.round(days);
        return rounded + (rounded == 1 ? " dia" : " dias");
    }

    private String formatStockAge(StockEntry entry) {
        long now = System.currentTimeMillis();
        long diff = Math.max(0, now - entry.addedAt);
        if (diff < 86400000L) {
            return formatShortDate(entry.addedAt) + " - " + formatTime(entry.addedAt);
        }
        long days = diff / 86400000L;
        return formatShortDate(entry.addedAt) + " - H\u00e1 " + days + (days == 1 ? " dia" : " dias");
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
        return formatShortDate(when) + " \u00e0s " + formatTime(when);
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
        if (Build.VERSION.SDK_INT >= 21) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(true);
        }
        if (Build.VERSION.SDK_INT >= 29) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }
        int statusColor = statusBarThemeColor();
        window.setStatusBarColor(statusColor);
        window.setNavigationBarColor(screenBg());
        window.getDecorView().setBackgroundColor(statusColor);
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = window.getDecorView().getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            flags &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (isLightColor(statusColor)) {
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
        int checked = isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent();
        int unchecked = isDarkTheme() ? withAlpha(CheckMercadoNeonUi.GREEN, 230) : Color.rgb(71, 85, 105);
        int[] colors = new int[]{checked, unchecked};
        box.setButtonTintList(new ColorStateList(states, colors));
        box.setTextColor(primaryText());
    }

    private void toggleTheme() {
        themeMode = isDarkTheme() ? THEME_LIGHT : THEME_DARK;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_THEME, themeMode).apply();
        if (selectedIndex >= 0) {
            showListScreen();
        } else if (homeTab == 1) {
            showStockWindow(false);
        } else if (homeTab == 2) {
            showStockWindow(true);
        } else if (homeTab == 3) {
            showHistoryScreen();
        } else if (homeTab == 4) {
            showMarketGame();
        } else if (homeTab == 6) {
            showStockHistoryWindow();
        } else if (homeTab == 7) {
            showCompraInvaders();
        } else if (homeTab == 9) {
            showMonthlyBudgetScreen();
        } else {
            showHomeScreen();
        }
    }

    private int statusBarThemeColor() {
        return isDarkTheme() ? darken(accentColor) : blend(accentColor, Color.WHITE, 0.18f);
    }

    private String themeIcon() {
        return isDarkTheme() ? "\u2600" : "\u263e";
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

    private boolean neonHome() {
        return isDarkTheme() && selectedIndex < 0 && homeTab == 0;
    }

    private void applyNeonIconButton(ImageButton button, int color) {
        button.setBackground(CheckMercadoNeonUi.iconButton(this, color));
        button.setColorFilter(CheckMercadoNeonUi.TEXT);
        elevate(button, 7);
    }

    private LinearLayout iconStrip() {
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        strip.setGravity(Gravity.CENTER);
        strip.setPadding(dp(5), dp(5), dp(5), dp(5));
        strip.setBackground(isDarkTheme()
                ? CheckMercadoNeonUi.panel(this)
                : inputPanelBg(false));
        elevate(strip, 5);
        return strip;
    }

    private ImageButton addStripIcon(LinearLayout strip, int drawable, int color, boolean enabled, int width, View.OnClickListener listener) {
        if (strip.getChildCount() > 0) addStripDivider(strip);
        ImageButton button = plainIconButton(drawable, enabled ? color : disabledText(), dp(9));
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1.0f : 0.38f);
        if (listener != null) button.setOnClickListener(listener);
        strip.addView(button, new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
        return button;
    }

    private ImageButton addWeightedStripIcon(LinearLayout strip, int drawable, int color, boolean enabled, View.OnClickListener listener) {
        if (strip.getChildCount() > 0) addStripDivider(strip);
        ImageButton button = plainIconButton(drawable, enabled ? color : disabledText(), dp(9));
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1.0f : 0.38f);
        if (listener != null) button.setOnClickListener(listener);
        strip.addView(button, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return button;
    }

    private void addStripDivider(LinearLayout strip) {
        View divider = new View(this);
        divider.setBackgroundColor(softDividerColor());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(1), ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(dp(3), dp(10), dp(3), dp(10));
        strip.addView(divider, params);
    }

    private ImageButton plainIconButton(int drawable, int fg, int padding) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(drawable);
        button.setColorFilter(fg);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(padding, padding, padding, padding);
        button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return button;
    }

    private ImageButton moreMenuButton(int fg) {
        ImageButton button = plainIconButton(R.drawable.ic_more_vertical, fg, dp(2));
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return button;
    }

    private int softDividerColor() {
        return isDarkTheme() ? Color.argb(86, 76, 201, 240) : Color.argb(115, 148, 163, 184);
    }

    private GradientDrawable softPillBg(int color) {
        return round(isDarkTheme() ? withAlpha(color, 36) : withAlpha(color, 28), dp(10), Color.TRANSPARENT, 0);
    }

    private int screenBg() {
        return isDarkTheme() ? Color.rgb(2, 6, 23) : Color.rgb(248, 250, 252);
    }

    private int cardBg() {
        return isDarkTheme() ? Color.argb(224, 8, 18, 38) : Color.argb(242, 255, 255, 255);
    }

    private int checkedBg() {
        return isDarkTheme() ? Color.argb(218, 30, 41, 59) : Color.argb(236, 241, 245, 249);
    }

    private int inputBg() {
        return isDarkTheme() ? Color.argb(208, 15, 23, 42) : Color.argb(238, 248, 250, 252);
    }

    private int softButtonBg() {
        return isDarkTheme() ? Color.rgb(30, 41, 59) : Color.rgb(226, 232, 240);
    }

    private int stroke() {
        return isDarkTheme()
                ? blend(accent(), Color.rgb(59, 130, 246), 0.36f)
                : blend(accent(), Color.rgb(148, 163, 184), 0.18f);
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
        return new String[]{"Mercado", "Hortifruti", "Prote\u00ednas", "Limpeza", "Higiene", "Farm\u00e1cia", "Bebidas", "Pet", "Outros", CUSTOM_CATEGORY};
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

    private int randomListColor() {
        float[] hsv = new float[]{
                (float) (Math.random() * 360.0),
                0.52f + (float) (Math.random() * 0.26),
                isDarkTheme() ? 0.56f + (float) (Math.random() * 0.18) : 0.72f + (float) (Math.random() * 0.16)
        };
        return Color.HSVToColor(hsv);
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

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
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
        button.setBackground(outlineButtonBg(bg, dp(14)));
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
        button.setBackground(outlineButtonBg(bg, dp(14)));
        button.setPadding(dp(11), dp(11), dp(11), dp(11));
        button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        elevate(button, 6);
        return button;
    }

    private ImageButton homeImageIconButton(int drawable, int bg, int fg) {
        ImageButton button = imageIconButton(drawable, bg, fg);
        button.setPadding(dp(12), dp(12), dp(12), dp(12));
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
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

    private GradientDrawable glassPanelBg() {
        int base = cardBg();
        int top = isDarkTheme()
                ? withAlpha(blend(accent(), Color.rgb(18, 31, 58), 0.12f), 178)
                : withAlpha(Color.WHITE, 178);
        int middle = isDarkTheme()
                ? withAlpha(blend(accent(), Color.rgb(8, 18, 38), 0.08f), 170)
                : withAlpha(blend(accent(), Color.WHITE, 0.05f), 170);
        int bottom = isDarkTheme()
                ? withAlpha(Color.rgb(7, 14, 31), 176)
                : withAlpha(blend(accent(), Color.rgb(241, 245, 249), 0.06f), 176);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{top, middle, bottom}
        );
        drawable.setCornerRadius(dp(24));
        drawable.setStroke(dp(1), blend(accent(), stroke(), isDarkTheme() ? 0.34f : 0.18f));
        return drawable;
    }

    private GradientDrawable glassCardBg(int tint) {
        int baseTint = tint == 0 ? accent() : tint;
        int top = isDarkTheme()
                ? withAlpha(blend(baseTint, Color.rgb(21, 33, 56), tint == 0 ? 0.08f : 0.28f), 178)
                : withAlpha(blend(baseTint, Color.WHITE, tint == 0 ? 0.03f : 0.18f), 178);
        int bottom = isDarkTheme()
                ? withAlpha(blend(baseTint, Color.rgb(8, 15, 31), tint == 0 ? 0.05f : 0.18f), 170)
                : withAlpha(blend(baseTint, Color.rgb(241, 245, 249), tint == 0 ? 0.02f : 0.12f), 170);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{top, bottom}
        );
        drawable.setCornerRadius(dp(18));
        drawable.setStroke(dp(1), tint == 0 ? stroke() : blend(baseTint, stroke(), isDarkTheme() ? 0.72f : 0.45f));
        return drawable;
    }

    private GradientDrawable selectedCardBg() {
        int color = accent();
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        withAlpha(blend(color, Color.WHITE, isDarkTheme() ? 0.35f : 0.24f), isDarkTheme() ? 236 : 246),
                        withAlpha(blend(color, screenBg(), isDarkTheme() ? 0.32f : 0.16f), isDarkTheme() ? 228 : 238)
                }
        );
        drawable.setCornerRadius(dp(18));
        drawable.setStroke(dp(2), color);
        return drawable;
    }

    private GradientDrawable checkedItemBg() {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        isDarkTheme() ? Color.argb(178, 28, 39, 56) : Color.argb(178, 236, 241, 247),
                        isDarkTheme() ? Color.argb(170, 18, 27, 43) : Color.argb(170, 226, 232, 240)
                }
        );
        drawable.setCornerRadius(dp(18));
        drawable.setStroke(dp(1), stroke());
        return drawable;
    }

    private GradientDrawable inputPanelBg(boolean active) {
        int border = active ? accent() : stroke();
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        isDarkTheme() ? Color.argb(178, 18, 29, 49) : Color.argb(178, 255, 255, 255),
                        isDarkTheme() ? Color.argb(170, 31, 43, 65) : Color.argb(170, 241, 245, 249)
                }
        );
        drawable.setCornerRadius(dp(18));
        drawable.setStroke(active ? dp(2) : dp(1), border);
        return drawable;
    }

    private int menuButtonBg() {
        return isDarkTheme() ? blend(accent(), Color.rgb(15, 23, 42), 0.46f) : blend(accent(), Color.WHITE, 0.34f);
    }

    private GradientDrawable glowRound(int color, int radius) {
        int top = blend(Color.WHITE, color, isDarkTheme() ? 0.26f : 0.46f);
        int mid = color;
        int bottom = darken(color);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{top, mid, bottom}
        );
        drawable.setCornerRadius(radius);
        drawable.setStroke(dp(1), blend(Color.WHITE, color, isDarkTheme() ? 0.42f : 0.58f));
        return drawable;
    }

    private GradientDrawable outlineButtonBg(int color, int radius) {
        int top = isDarkTheme()
                ? withAlpha(blend(color, Color.rgb(18, 31, 58), 0.18f), 148)
                : withAlpha(blend(color, Color.WHITE, 0.08f), 132);
        int bottom = isDarkTheme()
                ? withAlpha(blend(color, Color.rgb(8, 18, 38), 0.12f), 124)
                : withAlpha(blend(color, Color.rgb(241, 245, 249), 0.06f), 118);
        int border = isDarkTheme()
                ? blend(color, CheckMercadoNeonUi.BLUE, 0.34f)
                : blend(color, Color.rgb(37, 99, 235), 0.24f);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{top, bottom}
        );
        drawable.setCornerRadius(radius);
        drawable.setStroke(dp(1), withAlpha(border, isDarkTheme() ? 210 : 185));
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

    private LinearLayout.LayoutParams matchHeightWithTop(int height, int top) {
        LinearLayout.LayoutParams params = matchHeight(height);
        params.setMargins(0, top, 0, 0);
        return params;
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

    private int homeButtonSize() {
        return dp(72);
    }

    private int homeMainActionSize(int count) {
        int available = getResources().getDisplayMetrics().widthPixels - dp(96);
        int totalMargins = dp(8) * Math.max(0, count - 1);
        int fitted = (available - totalMargins) / Math.max(1, count);
        return Math.max(dp(54), Math.min(homeButtonSize(), fitted));
    }

    private int listActionButtonSize(boolean locked) {
        int count = locked ? 4 : 5;
        int available = getResources().getDisplayMetrics().widthPixels - dp(72);
        int totalMargins = dp(8) * (count - 1);
        int fitted = (available - totalMargins) / count;
        return Math.max(dp(42), Math.min(homeButtonSize(), fitted));
    }

    private static class PriceHit {
        final String listName;
        final String itemName;
        final double price;
        final long updatedAt;
        final long purchaseAt;

        PriceHit(String listName, String itemName, double price, long updatedAt, long purchaseAt) {
            this.listName = listName;
            this.itemName = itemName;
            this.price = price;
            this.updatedAt = updatedAt;
            this.purchaseAt = purchaseAt;
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

    private static class StockDurationStats {
        final String name;
        int cycles;
        double totalDays;
        double minDays;
        double maxDays;
        double lastDays;
        double totalQuantity;
        long lastAt;

        StockDurationStats(String name) {
            this.name = name;
        }

        double averageDays() {
            return cycles == 0 ? 0 : totalDays / cycles;
        }
    }

    private static class ShoppingList {
        String id = UUID.randomUUID().toString();
        String name;
        int color;
        double budget;
        long createdAt = System.currentTimeMillis();
        long lockedAt;
        boolean saveCheckedToStock = true;
        boolean locked;
        boolean archived;
        boolean deletedFromHistory;
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
            json.put("budget", budget);
            json.put("createdAt", createdAt);
            json.put("lockedAt", lockedAt);
            json.put("saveCheckedToStock", saveCheckedToStock);
            json.put("locked", locked);
            json.put("archived", archived);
            json.put("deletedFromHistory", deletedFromHistory);
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
            list.budget = json.optDouble("budget", 0);
            list.createdAt = json.optLong("createdAt", System.currentTimeMillis());
            list.lockedAt = json.optLong("lockedAt", 0);
            list.saveCheckedToStock = json.optBoolean("saveCheckedToStock", true);
            list.locked = json.optBoolean("locked", false);
            list.archived = json.optBoolean("archived", false);
            list.deletedFromHistory = json.optBoolean("deletedFromHistory", false);
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

    private class BrandLogoView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        BrandLogoView(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int h = getHeight();
            int icon = Math.max(dp(32), Math.min(dp(42), h - dp(12)));
            float left = dp(2);
            float top = (h - icon) / 2f;
            int color = accent();

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(color);
            paint.setShadowLayer(dp(7), 0, dp(2), Color.argb(isDarkTheme() ? 190 : 90, Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawLine(left + icon * 0.12f, top + icon * 0.22f, left + icon * 0.26f, top + icon * 0.72f, paint);
            canvas.drawLine(left + icon * 0.26f, top + icon * 0.72f, left + icon * 0.76f, top + icon * 0.72f, paint);
            canvas.drawLine(left + icon * 0.30f, top + icon * 0.34f, left + icon * 0.88f, top + icon * 0.34f, paint);
            canvas.drawLine(left + icon * 0.88f, top + icon * 0.34f, left + icon * 0.78f, top + icon * 0.58f, paint);
            canvas.drawCircle(left + icon * 0.36f, top + icon * 0.88f, dp(3), paint);
            canvas.drawCircle(left + icon * 0.72f, top + icon * 0.88f, dp(3), paint);

            paint.clearShadowLayer();
            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(dp(25));
            float textX = left + icon + dp(10);
            float baseline = h / 2f - (paint.descent() + paint.ascent()) / 2f;
            paint.setShader(new LinearGradient(textX, 0, Math.max(textX + dp(210), getWidth()), 0,
                    new int[]{lighten(color), color, Color.rgb(56, 189, 248)}, null, Shader.TileMode.CLAMP));
            paint.setShadowLayer(dp(5), 0, dp(2), Color.argb(isDarkTheme() ? 150 : 70, 0, 0, 0));
            canvas.drawText("Check Mercado", textX, baseline, paint);
            paint.setShader(null);
            paint.clearShadowLayer();
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

    private interface SearchCallback {
        void onSearchChanged(String value);
    }

    private interface QrReadCallback {
        void onQrRead(String value);
    }

    private class QrFrameOverlay extends View {
        private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint shade = new Paint(Paint.ANTI_ALIAS_FLAG);

        QrFrameOverlay(Context context) {
            super(context);
            line.setStyle(Paint.Style.STROKE);
            line.setStrokeWidth(dp(4));
            line.setColor(Color.WHITE);
            shade.setColor(Color.argb(92, 0, 0, 0));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int size = Math.min(getWidth() - dp(48), getHeight() - dp(170));
            size = Math.max(dp(220), Math.min(size, dp(360)));
            int left = (getWidth() - size) / 2;
            int top = Math.max(dp(40), (getHeight() - size) / 2 - dp(28));
            int right = left + size;
            int bottom = top + size;
            canvas.drawRect(0, 0, getWidth(), top, shade);
            canvas.drawRect(0, bottom, getWidth(), getHeight(), shade);
            canvas.drawRect(0, top, left, bottom, shade);
            canvas.drawRect(right, top, getWidth(), bottom, shade);

            int corner = dp(44);
            canvas.drawLine(left, top, left + corner, top, line);
            canvas.drawLine(left, top, left, top + corner, line);
            canvas.drawLine(right, top, right - corner, top, line);
            canvas.drawLine(right, top, right, top + corner, line);
            canvas.drawLine(left, bottom, left + corner, bottom, line);
            canvas.drawLine(left, bottom, left, bottom - corner, line);
            canvas.drawLine(right, bottom, right - corner, bottom, line);
            canvas.drawLine(right, bottom, right, bottom - corner, line);
        }
    }

    private class CompraInvadersView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final TextView status;
        private final boolean[][] alive = new boolean[4][7];
        private final int[][] health = new int[4][7];
        private final Runnable tick = new Runnable() {
            @Override
            public void run() {
                updateGame();
                invalidate();
                if (running) handler.postDelayed(this, 34);
            }
        };
        private boolean running;
        private boolean gameOver;
        private int playerCol = 3;
        private int bulletCol = -1;
        private float bulletY;
        private float invaderX;
        private float invaderY;
        private float direction = 1;
        private int score;
        private int wave = 1;
        private int bestScore;

        CompraInvadersView(Context context, TextView status) {
            super(context);
            this.status = status;
            bestScore = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_INVADERS_BEST, 0);
            setBackground(round(cardBg(), dp(18), stroke(), 1));
            resetWave();
        }

        void start() {
            running = true;
            handler.removeCallbacks(tick);
            handler.post(tick);
        }

        void stop() {
            running = false;
            handler.removeCallbacks(tick);
        }

        void movePlayer(int delta) {
            if (gameOver) resetGame();
            playerCol = Math.max(0, Math.min(6, playerCol + delta));
            invalidate();
        }

        void fire() {
            if (gameOver) {
                resetGame();
                return;
            }
            if (bulletCol < 0) {
                bulletCol = playerCol;
                bulletY = getHeight() - dp(72);
            }
        }

        private void resetGame() {
            score = 0;
            wave = 1;
            gameOver = false;
            resetWave();
        }

        private void resetWave() {
            for (int r = 0; r < alive.length; r++) {
                for (int c = 0; c < alive[r].length; c++) {
                    alive[r][c] = true;
                    health[r][c] = 1 + Math.max(0, wave - 1) / 2 + (r == 0 && wave >= 3 ? 1 : 0);
                }
            }
            invaderX = dp(18);
            invaderY = dp(34);
            direction = 1;
            bulletCol = -1;
            updateStatus();
        }

        private void updateGame() {
            if (gameOver || getWidth() <= 0) return;
            invaderX += direction * (1.5f + wave * 0.55f);
            float formationWidth = 7 * cell();
            if (invaderX < dp(8) || invaderX + formationWidth > getWidth() - dp(8)) {
                direction *= -1;
                invaderY += dp(16);
            }
            if (bulletCol >= 0) {
                bulletY -= dp(8);
                if (bulletY < 0) bulletCol = -1;
                else hitTest();
            }
            if (liveInvaderBottom() > getHeight() - dp(86)) {
                gameOver = true;
                saveBestIfNeeded();
                status.setText("Fim de jogo - toque em Atirar para reiniciar. Pontos: " + score);
            }
        }

        private float liveInvaderBottom() {
            float bottom = 0;
            for (int r = 0; r < alive.length; r++) {
                for (int c = 0; c < alive[r].length; c++) {
                    if (alive[r][c]) bottom = Math.max(bottom, invaderY + (r + 1) * cell());
                }
            }
            return bottom;
        }

        private void hitTest() {
            float x = laneX(bulletCol);
            for (int r = 0; r < alive.length; r++) {
                for (int c = 0; c < alive[r].length; c++) {
                    if (!alive[r][c]) continue;
                    float left = invaderX + c * cell();
                    float top = invaderY + r * cell();
                    if (x >= left && x <= left + cell() && bulletY >= top && bulletY <= top + cell()) {
                        bulletCol = -1;
                        health[r][c]--;
                        if (health[r][c] > 0) {
                            score += 3 * wave;
                            updateStatus();
                            return;
                        }
                        alive[r][c] = false;
                        score += 10 * wave;
            if (cleared()) {
                saveBestIfNeeded();
                wave++;
                resetWave();
                        } else {
                            updateStatus();
                        }
                        return;
                    }
                }
            }
        }

        private boolean cleared() {
            for (boolean[] row : alive) {
                for (boolean cell : row) if (cell) return false;
            }
            return true;
        }

        private void updateStatus() {
            status.setText("Onda " + wave + " - Pontos " + score + " - Recorde " + bestScore);
        }

        private void saveBestIfNeeded() {
            if (score <= bestScore) return;
            bestScore = score;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_INVADERS_BEST, bestScore).apply();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(isDarkTheme() ? Color.rgb(15, 23, 42) : Color.WHITE);
            canvas.drawRoundRect(0, 0, getWidth(), getHeight(), dp(18), dp(18), paint);
            drawShelves(canvas);
            drawInvaders(canvas);
            drawPlayer(canvas);
            drawBullet(canvas);
            if (gameOver) drawGameOver(canvas);
        }

        private void drawShelves(Canvas canvas) {
            paint.setColor(isDarkTheme() ? Color.rgb(51, 65, 85) : Color.rgb(226, 232, 240));
            for (int i = 0; i < 3; i++) {
                float y = dp(86 + i * 76);
                canvas.drawRoundRect(dp(18), y, getWidth() - dp(18), y + dp(10), dp(5), dp(5), paint);
            }
        }

        private void drawInvaders(Canvas canvas) {
            float size = cell();
            for (int r = 0; r < alive.length; r++) {
                for (int c = 0; c < alive[r].length; c++) {
                    if (!alive[r][c]) continue;
                    float left = invaderX + c * size;
                    float top = invaderY + r * size;
                    paint.setColor(productColor(r, c));
                    canvas.drawRoundRect(left + dp(6), top + dp(8), left + size - dp(6), top + size - dp(8), dp(8), dp(8), paint);
                    if (health[r][c] > 1) {
                        paint.setColor(Color.rgb(250, 204, 21));
                        canvas.drawCircle(left + size * 0.5f, top + size * 0.22f, dp(4), paint);
                    }
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(left + size * 0.35f, top + size * 0.42f, dp(3), paint);
                    canvas.drawCircle(left + size * 0.65f, top + size * 0.42f, dp(3), paint);
                }
            }
        }

        private void drawPlayer(Canvas canvas) {
            float x = laneX(playerCol);
            float y = getHeight() - dp(48);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(4));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(accent());
            canvas.drawLine(x - dp(22), y - dp(12), x + dp(18), y - dp(12), paint);
            canvas.drawLine(x - dp(15), y - dp(12), x - dp(5), y + dp(10), paint);
            canvas.drawLine(x - dp(5), y + dp(10), x + dp(24), y + dp(10), paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x - dp(2), y + dp(18), dp(5), paint);
            canvas.drawCircle(x + dp(20), y + dp(18), dp(5), paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
        }

        private void drawBullet(Canvas canvas) {
            if (bulletCol < 0) return;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(250, 204, 21));
            float x = laneX(bulletCol);
            canvas.drawRoundRect(x - dp(3), bulletY - dp(14), x + dp(3), bulletY, dp(3), dp(3), paint);
        }

        private void drawGameOver(Canvas canvas) {
            paint.setColor(isDarkTheme() ? Color.WHITE : Color.rgb(15, 23, 42));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(dp(22));
            canvas.drawText("Fim de jogo", getWidth() / 2f, getHeight() / 2f, paint);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextSize(dp(14));
            canvas.drawText("Atirar reinicia", getWidth() / 2f, getHeight() / 2f + dp(24), paint);
        }

        private float cell() {
            return Math.max(dp(34), Math.min(dp(46), (getWidth() - dp(52)) / 7f));
        }

        private float laneX(int col) {
            float margin = dp(26);
            return margin + col * ((getWidth() - 2 * margin) / 6f);
        }

        private int productColor(int row, int col) {
            int[] colors = new int[]{
                    Color.rgb(239, 68, 68),
                    Color.rgb(22, 163, 74),
                    Color.rgb(37, 99, 235),
                    Color.rgb(234, 88, 12),
                    Color.rgb(147, 51, 234)
            };
            return colors[Math.abs(row * 7 + col) % colors.length];
        }
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

    private class QrScannerView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
        private final QrReadCallback callback;
        private final MultiFormatReader reader = new MultiFormatReader();
        private final BarcodeScanner mlScanner;
        private Camera camera;
        private boolean decoded;
        private boolean mlBusy;
        private long lastDecodeAt;
        private long lastFocusAt;
        private boolean previewing;
        private final Handler focusHandler = new Handler(Looper.getMainLooper());
        private final Runnable focusPulse = new Runnable() {
            @Override
            public void run() {
                if (camera == null || decoded) return;
                focusCamera();
                focusHandler.postDelayed(this, 2800);
            }
        };

        QrScannerView(Context context, QrReadCallback callback) {
            super(context);
            this.callback = callback;
            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build();
            mlScanner = BarcodeScanning.getClient(options);
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            reader.setHints(hints);
            getHolder().addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            start(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stop();
        }

        private void start(SurfaceHolder holder) {
            try {
                camera = Camera.open();
                Camera.Parameters params = camera.getParameters();
                configureCamera(params);
                Camera.Size size = bestPreviewSize(params.getSupportedPreviewSizes());
                if (size != null) params.setPreviewSize(size.width, size.height);
                camera.setParameters(params);
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(holder);
                camera.setPreviewCallback(this);
                camera.startPreview();
                previewing = true;
                focusHandler.postDelayed(() -> focusCamera(true), 350);
                focusHandler.postDelayed(() -> focusCamera(true), 1100);
                focusHandler.postDelayed(focusPulse, 2800);
            } catch (Exception e) {
                stop();
                promptFiscalQrUrl();
            }
        }

        private void configureCamera(Camera.Parameters params) {
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            } else if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            Camera.Area center = new Camera.Area(new Rect(-280, -280, 280, 280), 1000);
            if (params.getMaxNumFocusAreas() > 0) {
                params.setFocusAreas(Collections.singletonList(center));
            }
            if (params.getMaxNumMeteringAreas() > 0) {
                params.setMeteringAreas(Collections.singletonList(center));
            }
        }

        private Camera.Size bestPreviewSize(List<Camera.Size> sizes) {
            if (sizes == null || sizes.isEmpty()) return null;
            Camera.Size best = sizes.get(0);
            for (Camera.Size size : sizes) {
                int pixels = size.width * size.height;
                int bestPixels = best.width * best.height;
                if (pixels > bestPixels && pixels <= 1920 * 1080) best = size;
            }
            return best;
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (decoded || data == null || camera == null) return;
            long now = System.currentTimeMillis();
            if (now - lastFocusAt > 3600) focusCamera();
            if (now - lastDecodeAt < 110) return;
            lastDecodeAt = now;
            try {
                Camera.Size size = camera.getParameters().getPreviewSize();
                decodeWithMlKit(data, size.width, size.height);
                Result result = decodeFrame(data, size.width, size.height);
                if (result != null && result.getText() != null) {
                    decoded = true;
                    post(() -> callback.onQrRead(result.getText()));
                }
            } catch (Exception ignored) {
            } finally {
                reader.reset();
            }
        }

        private void decodeWithMlKit(byte[] data, int width, int height) {
            if (mlBusy || decoded) return;
            mlBusy = true;
            byte[] copy = data.clone();
            InputImage image = InputImage.fromByteArray(copy, width, height, 90, InputImage.IMAGE_FORMAT_NV21);
            mlScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (decoded || barcodes == null) return;
                        for (Barcode barcode : barcodes) {
                            String value = barcode.getRawValue();
                            if (value != null && !value.trim().isEmpty()) {
                                decoded = true;
                                callback.onQrRead(value);
                                break;
                            }
                        }
                    })
                    .addOnCompleteListener(task -> mlBusy = false);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                focusAt(event.getX(), event.getY());
                return true;
            }
            return true;
        }

        private void focusAt(float x, float y) {
            if (camera == null || getWidth() <= 0 || getHeight() <= 0) return;
            try {
                int focusX = clamp((int) (x / getWidth() * 2000 - 1000), -1000, 1000);
                int focusY = clamp((int) (y / getHeight() * 2000 - 1000), -1000, 1000);
                Rect area = new Rect(
                        clamp(focusX - 220, -1000, 1000),
                        clamp(focusY - 220, -1000, 1000),
                        clamp(focusX + 220, -1000, 1000),
                        clamp(focusY + 220, -1000, 1000)
                );
                Camera.Parameters params = camera.getParameters();
                if (params.getMaxNumFocusAreas() > 0) {
                    params.setFocusAreas(Collections.singletonList(new Camera.Area(area, 1000)));
                }
                if (params.getMaxNumMeteringAreas() > 0) {
                    params.setMeteringAreas(Collections.singletonList(new Camera.Area(area, 1000)));
                }
                List<String> focusModes = params.getSupportedFocusModes();
                if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                camera.setParameters(params);
                focusCamera(true);
            } catch (Exception ignored) {
                focusCamera(true);
            }
        }

        private int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }

        private void focusCamera() {
            focusCamera(false);
        }

        private void focusCamera(boolean force) {
            if (camera == null || !previewing) return;
            long now = System.currentTimeMillis();
            if (!force && now - lastFocusAt < 2300) return;
            lastFocusAt = System.currentTimeMillis();
            try {
                camera.cancelAutoFocus();
            } catch (Exception ignored) {
            }
            try {
                camera.autoFocus((success, camera) -> {
                    try {
                        if (camera != null) camera.cancelAutoFocus();
                    } catch (Exception ignored) {
                    }
                });
            } catch (Exception ignored) {
            }
        }

        private Result decodeFrame(byte[] data, int width, int height) {
            Result result = decodeYuv(data, width, height, false);
            if (result != null) return result;
            result = decodeYuv(data, width, height, true);
            if (result != null) return result;
            byte[] rotated = rotateYuv90(data, width, height);
            result = decodeYuv(rotated, height, width, false);
            if (result != null) return result;
            result = decodeYuv(rotated, height, width, true);
            if (result != null) return result;
            byte[] inverted = invertY(data);
            result = decodeYuv(inverted, width, height, false);
            if (result != null) return result;
            byte[] rotatedInverted = rotateYuv90(inverted, width, height);
            return decodeYuv(rotatedInverted, height, width, true);
        }

        private Result decodeYuv(byte[] data, int width, int height, boolean centerOnly) {
            int left = 0;
            int top = 0;
            int cropWidth = width;
            int cropHeight = height;
            if (centerOnly) {
                int crop = Math.min(width, height) * 4 / 5;
                left = (width - crop) / 2;
                top = (height - crop) / 2;
                cropWidth = crop;
                cropHeight = crop;
            }
            try {
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, left, top, cropWidth, cropHeight, false);
                return reader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
            } catch (Exception ignored) {
                try {
                    PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, left, top, cropWidth, cropHeight, false);
                    return reader.decodeWithState(new BinaryBitmap(new GlobalHistogramBinarizer(source)));
                } catch (Exception ignoredAgain) {
                    return null;
                }
            } finally {
                reader.reset();
            }
        }

        private byte[] rotateYuv90(byte[] data, int width, int height) {
            byte[] rotated = new byte[data.length];
            int index = 0;
            for (int x = 0; x < width; x++) {
                for (int y = height - 1; y >= 0; y--) {
                    rotated[index++] = data[y * width + x];
                }
            }
            int frameSize = width * height;
            for (int i = frameSize; i < data.length && index < rotated.length; i++) {
                rotated[index++] = data[i];
            }
            return rotated;
        }

        private byte[] invertY(byte[] data) {
            byte[] inverted = data.clone();
            int ySize = inverted.length * 2 / 3;
            for (int i = 0; i < ySize && i < inverted.length; i++) {
                inverted[i] = (byte) (255 - (inverted[i] & 0xff));
            }
            return inverted;
        }

        void stop() {
            focusHandler.removeCallbacksAndMessages(null);
            previewing = false;
            try {
                if (camera != null) {
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                }
            } catch (Exception ignored) {
            }
            try {
                mlScanner.close();
            } catch (Exception ignored) {
            }
            camera = null;
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
                        text.setTextColor(isDarkTheme() ? Color.rgb(226, 232, 240) : primaryText());
                        text.setTextSize(16);
                        text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
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
        public AlertDialog.Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, DialogInterface.OnClickListener listener) {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(MainActivity.this, android.R.layout.simple_list_item_single_choice, items) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    view.setBackgroundColor(cardBg());
                    view.setPadding(dp(10), dp(7), dp(10), dp(7));
                    if (view instanceof TextView) {
                        TextView text = (TextView) view;
                        text.setTextColor(position == checkedItem
                                ? (isDarkTheme() ? CheckMercadoNeonUi.GREEN : accent())
                                : (isDarkTheme() ? Color.rgb(226, 232, 240) : primaryText()));
                        text.setTextSize(16);
                        text.setTypeface(Typeface.DEFAULT, position == checkedItem ? Typeface.BOLD : Typeface.NORMAL);
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    return getView(position, convertView, parent);
                }
            };
            return setSingleChoiceItems(adapter, checkedItem, listener);
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
        String note = "";
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
            json.put("note", note);
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
            item.note = json.optString("note", "");
            item.updatedAt = json.optLong("updatedAt", System.currentTimeMillis());
            item.stockId = json.optString("stockId", "");
            return item;
        }
    }

    private static class MonthlyBudgetIncome {
        String id = UUID.randomUUID().toString();
        String description = "";
        double amount;
        String monthKey = "";
        long createdAt = System.currentTimeMillis();
        long updatedAt = createdAt;

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("description", description);
            json.put("amount", amount);
            json.put("monthKey", monthKey);
            json.put("createdAt", createdAt);
            json.put("updatedAt", updatedAt);
            return json;
        }

        static MonthlyBudgetIncome fromJson(JSONObject json) {
            MonthlyBudgetIncome income = new MonthlyBudgetIncome();
            income.id = json.optString("id", UUID.randomUUID().toString());
            income.description = json.optString("description", "Entrada");
            income.amount = json.optDouble("amount", 0);
            income.monthKey = json.optString("monthKey", "");
            income.createdAt = json.optLong("createdAt", System.currentTimeMillis());
            income.updatedAt = json.optLong("updatedAt", income.createdAt);
            return income;
        }
    }
    private static class MonthlyBudgetEntry {
        String id = UUID.randomUUID().toString();
        String description = "";
        String installment = "";
        double amount;
        int dueDay = 5;
        boolean paid;
        String monthKey = "";
        String paidAt = "";
        String paymentMethod = "";
        String category = "";
        long createdAt = System.currentTimeMillis();
        long updatedAt = createdAt;

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("description", description);
            json.put("installment", installment);
            json.put("amount", amount);
            json.put("dueDay", dueDay);
            json.put("paid", paid);
            json.put("monthKey", monthKey);
            json.put("paidAt", paidAt);
            json.put("paymentMethod", paymentMethod);
            json.put("category", category);
            json.put("createdAt", createdAt);
            json.put("updatedAt", updatedAt);
            return json;
        }

        static MonthlyBudgetEntry fromJson(JSONObject json) {
            MonthlyBudgetEntry entry = new MonthlyBudgetEntry();
            entry.id = json.optString("id", UUID.randomUUID().toString());
            entry.description = json.optString("description", "Despesa");
            entry.installment = json.optString("installment", "");
            entry.amount = json.optDouble("amount", 0);
            entry.dueDay = json.optInt("dueDay", 5);
            entry.paid = json.optBoolean("paid", false);
            entry.monthKey = json.optString("monthKey", "");
            entry.paidAt = json.optString("paidAt", "");
            entry.paymentMethod = json.optString("paymentMethod", "");
            entry.category = json.optString("category", "");
            entry.createdAt = json.optLong("createdAt", System.currentTimeMillis());
            entry.updatedAt = json.optLong("updatedAt", entry.createdAt);
            return entry;
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
