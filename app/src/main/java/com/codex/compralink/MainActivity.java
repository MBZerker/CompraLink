package com.codex.compralink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import android.content.res.Configuration;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_ACCENT = "accent_color";
    private static final String KEY_LAST_CLIPBOARD_PAYLOAD = "last_clipboard_payload";
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

    private final List<ShoppingList> lists = new ArrayList<>();
    private final List<StockEntry> stock = new ArrayList<>();
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private LinearLayout root;
    private EditText itemInput;
    private EditText priceInput;
    private EditText unitInput;
    private int selectedIndex = -1;
    private int homeTab = 0;
    private int spendingRangeMonths = 6;
    private boolean shellReady;
    private boolean pendingIntentHandled;
    private int themeMode = THEME_SYSTEM;
    private int accentColor = Color.rgb(15, 118, 110);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeMode = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_THEME, THEME_SYSTEM);
        accentColor = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_ACCENT, Color.rgb(15, 118, 110));
        load();
        loadStock();
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
        buildRoot();
        addTopHeader("Suas listas", "Crie listas e compare precos salvos.", false);

        for (int i = 0; i < lists.size(); i++) {
            root.addView(listCard(i), matchWrapWithTop(dp(10)));
        }
        if (lists.isEmpty()) {
            root.addView(infoCard("Nenhuma lista criada", "Toque no carrinho para criar sua primeira lista."), matchWrapWithTop(dp(10)));
        }
        setContentView(rootScroll());
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
        buildRoot();
        ShoppingList list = lists.get(selectedIndex);
        addTopHeader(list.name, listSubtitle(list), true);
        addInputCard();

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
        root.addView(header, matchWrap());

        TextView appName = new TextView(this);
        appName.setText("CompraLink");
        appName.setTextColor(accent());
        appName.setTextSize(14);
        appName.setTypeface(Typeface.DEFAULT_BOLD);
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
            sort.setOnClickListener(v -> {
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
                LinearLayout.LayoutParams stockParams = new LinearLayout.LayoutParams(dp(48), dp(48));
                stockParams.setMargins(dp(8), 0, 0, 0);
                actions.addView(stockButton, stockParams);
            }

            if (homeTab == 0) {
                ImageButton update = imageIconButton(R.drawable.ic_update, isDarkTheme() ? Color.rgb(71, 85, 105) : Color.rgb(51, 65, 85), Color.WHITE);
                update.setOnClickListener(v -> UpdateManager.checkForUpdates(this, true));
                LinearLayout.LayoutParams updateParams = new LinearLayout.LayoutParams(dp(48), dp(48));
                updateParams.setMargins(dp(8), 0, 0, 0);
                actions.addView(update, updateParams);

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

    private View listCard(int index) {
        ShoppingList list = lists.get(index);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        int listColor = list.displayColor();
        card.setBackground(round(tintSurface(listColor), dp(16), listColor == 0 ? stroke() : listColor, 1));
        card.setOnClickListener(v -> {
            selectedIndex = index;
            showListScreen();
        });
        card.setOnLongClickListener(v -> {
            showListOptions(index);
            return true;
        });

        TextView name = new TextView(this);
        name.setText(list.name);
        name.setTextSize(19);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setTextColor(listColor == 0 ? primaryText() : readableOnTint(listColor));
        card.addView(name);

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
        return formatShortDate(list.createdAt) + " - " + list.items.size() + " itens, " + done + " concluidos, total " + money.format(total);
    }

    private void showPrintPreview() {
        if (selectedIndex < 0) return;
        ShoppingList list = lists.get(selectedIndex);
        applySystemBars();

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setPadding(dp(14), dp(14), dp(14), dp(18));
        screen.setBackgroundColor(screenBg());

        Button close = iconButton("X", softButtonBg(), primaryText());
        close.setTextSize(18);
        close.setOnClickListener(v -> showListScreen());
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        closeParams.gravity = Gravity.START;
        screen.addView(close, closeParams);

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

    private TextView printText(String text, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(Color.BLACK);
        view.setLineSpacing(dp(2), 1.0f);
        if (bold) view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
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
            TextView edited = label("Editado: " + formatDateTime(entry.updatedAt), 13, false, mutedText());
            edited.setPadding(0, dp(4), 0, 0);
            card.addView(edited);
            root.addView(card, matchWrapWithTop(dp(10)));
        }
    }

    private void addSpendingScreen() {
        Map<String, Double> totals = new LinkedHashMap<>();
        Map<String, SpendingProduct> products = new LinkedHashMap<>();
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        Calendar currentMonth = Calendar.getInstance();
        Calendar previousMonth = Calendar.getInstance();
        previousMonth.add(Calendar.MONTH, -1);
        double currentTotal = 0;
        double previousTotal = 0;
        StockEntry biggestEntry = null;
        int visibleMonths = spendingRangeMonths <= 0 ? Math.max(6, countMonthsWithData()) : spendingRangeMonths;
        for (int i = visibleMonths - 1; i >= 0; i--) {
            cal.setTimeInMillis(now);
            cal.add(Calendar.MONTH, -i);
            totals.put(monthKey(cal), 0.0);
        }
        for (StockEntry entry : stock) {
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

            String productKey = normalize(entry.name);
            SpendingProduct product = products.get(productKey);
            if (product == null) {
                product = new SpendingProduct(entry.name);
                products.put(productKey, product);
            }
            product.total += total;
            product.quantity += entry.quantity;
            product.times += 1;
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
        addMonthlyBars(totals, max);
        addProductRanking(products);
    }

    private void addSpendingFilters() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row, matchWrapWithTop(dp(10)));
        addSpendingFilterButton(row, "1m", 1);
        addSpendingFilterButton(row, "3m", 3);
        addSpendingFilterButton(row, "6m", 6);
        addSpendingFilterButton(row, "12m", 12);
        addSpendingFilterButton(row, "Tudo", 0);
    }

    private void addSpendingFilterButton(LinearLayout row, String label, int months) {
        Button button = button(label, spendingRangeMonths == months ? accent() : softButtonBg(), spendingRangeMonths == months ? Color.WHITE : primaryText());
        button.setTextSize(12);
        button.setOnClickListener(v -> {
            spendingRangeMonths = months;
            showStockWindow(true);
        });
        LinearLayout.LayoutParams params = weighted();
        if (row.getChildCount() > 0) params.setMargins(dp(5), 0, 0, 0);
        row.addView(button, params);
    }

    private void addMetricGrid(double currentTotal, double previousTotal, double difference, double average, double forecast, StockEntry biggestEntry, Map<String, SpendingProduct> products) {
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
        card.addView(label(title, 12, true, mutedText()));
        TextView valueView = label(value, 16, true, valueColor);
        valueView.setPadding(0, dp(5), 0, 0);
        valueView.setSingleLine(false);
        card.addView(valueView, matchWrap());
        return card;
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

    private boolean sameMonth(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.MONTH) == b.get(Calendar.MONTH);
    }

    private double forecastMonthTotal(double currentTotal) {
        Calendar today = Calendar.getInstance();
        int day = Math.max(1, today.get(Calendar.DAY_OF_MONTH));
        int maxDay = today.getActualMaximum(Calendar.DAY_OF_MONTH);
        return currentTotal / day * maxDay;
    }

    private int countMonthsWithData() {
        if (stock.isEmpty()) return 6;
        Calendar first = Calendar.getInstance();
        Calendar last = Calendar.getInstance();
        long min = Long.MAX_VALUE;
        long max = 0;
        for (StockEntry entry : stock) {
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

        itemInput = new EditText(this);
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
            promptEditItem(item);
            return true;
        });

        CheckBox box = new CheckBox(this);
        box.setChecked(item.checked);
        tintCheckBox(box);
        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                item.checked = true;
                if (lists.get(selectedIndex).saveCheckedToStock) {
                    addToStock(item, quantityOf(item), "un");
                }
                save();
                showListScreen();
            } else {
                item.checked = false;
                removeStockForItem(item);
                save();
                showListScreen();
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

        Button remove = button("x", Color.rgb(254, 226, 226), Color.rgb(153, 27, 27));
        remove.setTextSize(18);
        remove.setOnClickListener(v -> {
            lists.get(selectedIndex).items.remove(index);
            save();
            showListScreen();
        });
        row.addView(remove, new LinearLayout.LayoutParams(dp(42), dp(42)));
        return row;
    }

    private void addItem() {
        String text = itemInput.getText().toString().trim();
        if (text.isEmpty()) return;
        String unit = unitInput == null ? "" : unitInput.getText().toString().trim();
        if (unit.isEmpty()) unit = "1";
        lists.get(selectedIndex).items.add(new ShoppingItem(text, parsePrice(priceInput.getText().toString()), unit));
        save();
        hideKeyboard();
        showListScreen();
    }

    private void promptStockQuantity(ShoppingItem item, Runnable onSaved, Runnable onCancel) {
        LinearLayout form = dialogForm();
        EditText qty = dialogInput("Quantidade", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        qty.setText("1");
        EditText unit = dialogInput("Unidade", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        unit.setText(item.unit == null || item.unit.isEmpty() ? "un" : item.unit);
        form.addView(qty, matchHeight(dp(54)));
        form.addView(unit, matchWrapWithTop(dp(8)));
        new AlertDialog.Builder(this)
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
            item.stockId = linked.id;
            saveStock();
            return;
        }
        StockEntry entry = new StockEntry(item.name, amount, unit, item.price, now);
        entry.sourceItemId = item.id;
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
        new AlertDialog.Builder(this)
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
        new AlertDialog.Builder(this)
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

        new AlertDialog.Builder(this)
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
        new AlertDialog.Builder(this)
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
        String[] options = new String[]{"Editar nome", "Mudar cor", "Remover"};
        new AlertDialog.Builder(this)
                .setTitle(lists.get(index).name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        promptEditList(index);
                    } else if (which == 1) {
                        promptListColor(index);
                    } else {
                        confirmDeleteList(index);
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
        EditText input = dialogInput("Nome da lista", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(list.name);
        input.setSelection(input.getText().length());
        new AlertDialog.Builder(this)
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
        final int[] colors = new int[]{
                0,
                Color.rgb(15, 118, 110),
                Color.rgb(13, 148, 136),
                Color.rgb(6, 182, 212),
                Color.rgb(37, 99, 235),
                Color.rgb(59, 130, 246),
                Color.rgb(124, 58, 237),
                Color.rgb(147, 51, 234),
                Color.rgb(219, 39, 119),
                Color.rgb(225, 29, 72),
                Color.rgb(234, 88, 12),
                Color.rgb(245, 158, 11),
                Color.rgb(202, 138, 4),
                Color.rgb(22, 163, 74),
                Color.rgb(132, 204, 22),
                Color.rgb(71, 85, 105),
                Color.rgb(120, 113, 108)
        };
        LinearLayout grid = dialogForm();
        final AlertDialog[] dialogRef = new AlertDialog[1];
        Button lighter = button("Clarear cor atual", Color.rgb(226, 232, 240), Color.rgb(15, 23, 42));
        lighter.setOnClickListener(v -> {
            lists.get(index).color = lighten(lists.get(index).displayColor() == 0 ? accentColor : lists.get(index).displayColor());
            save();
            if (dialogRef[0] != null) dialogRef[0].dismiss();
            showHomeScreen();
        });
        grid.addView(lighter, matchHeight(dp(42)));

        Button darker = button("Escurecer cor atual", Color.rgb(51, 65, 85), Color.WHITE);
        darker.setOnClickListener(v -> {
            lists.get(index).color = darken(lists.get(index).displayColor() == 0 ? accentColor : lists.get(index).displayColor());
            save();
            if (dialogRef[0] != null) dialogRef[0].dismiss();
            showHomeScreen();
        });
        grid.addView(darker, matchWrapWithTop(dp(8)));

        for (int color : colors) {
            Button swatch = button(color == 0 ? "Padrao" : " ", color == 0 ? softButtonBg() : color, color == 0 ? primaryText() : Color.WHITE);
            swatch.setOnClickListener(v -> {
                lists.get(index).color = color;
                save();
                if (dialogRef[0] != null) dialogRef[0].dismiss();
                showHomeScreen();
            });
            grid.addView(swatch, matchHeight(dp(42)));
        }
        dialogRef[0] = new AlertDialog.Builder(this)
                .setTitle("Cor da lista")
                .setView(grid)
                .setNegativeButton("Fechar", null)
                .create();
        dialogRef[0].show();
    }

    private void confirmDeleteList(int index) {
        if (lists.size() == 1) {
            Toast.makeText(this, "Mantenha pelo menos uma lista.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Remover lista?")
                .setMessage(lists.get(index).name)
                .setPositiveButton("Remover", (dialog, which) -> {
                    lists.remove(index);
                    save();
                    showHomeScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmDeleteStock(StockEntry entry) {
        new AlertDialog.Builder(this)
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
        String[] options = new String[]{"Editar quantidade", "Remover"};
        new AlertDialog.Builder(this)
                .setTitle(entry.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        promptEditStockQuantity(entry);
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
        new AlertDialog.Builder(this)
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
        form.setPadding(dp(8), dp(8), dp(8), 0);
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
        window.setStatusBarColor(screenBg());
        window.setNavigationBarColor(screenBg());
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = window.getDecorView().getSystemUiVisibility();
            if (isDarkTheme()) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
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

    private Button button(String text, int bg, int fg) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(fg);
        button.setTextSize(14);
        button.setMinHeight(dp(48));
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackground(round(bg, dp(14), Color.TRANSPARENT, 0));
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
        button.setBackground(round(bg, dp(14), Color.TRANSPARENT, 0));
        button.setPadding(dp(11), dp(11), dp(11), dp(11));
        button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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

    private static class SpendingProduct {
        final String name;
        double total;
        double quantity;
        int times;

        SpendingProduct(String name) {
            this.name = name;
        }
    }

    private static class ShoppingList {
        String id = UUID.randomUUID().toString();
        String name;
        int color;
        long createdAt = System.currentTimeMillis();
        boolean saveCheckedToStock = true;
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
            json.put("saveCheckedToStock", saveCheckedToStock);
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
            list.saveCheckedToStock = json.optBoolean("saveCheckedToStock", true);
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
            return entry;
        }
    }
}
