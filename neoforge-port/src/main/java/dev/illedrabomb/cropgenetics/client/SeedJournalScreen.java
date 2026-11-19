package dev.illedrabomb.cropgenetics.client;

import dev.illedrabomb.cropgenetics.content.CropSpeciesNames;
import dev.illedrabomb.cropgenetics.content.SeedJournalItem;
import dev.illedrabomb.cropgenetics.content.SeedSpeciesCatalog;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.content.CropComponents;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * A standalone, client-side book for the server-saved journal entries.
 * No extra mod or custom network channel is required: the journal ItemStack
 * already synchronizes its data component to the client.
 */
public final class SeedJournalScreen extends Screen {
    private static final int ENTRIES_PER_PAGE = 7;
    private static final int GUIDE_PAGES = 3;
    private static final int MUTATIONS_PER_PAGE = 3;
    private final List<String> discovered;
    private final List<SeedJournalItem.MutationHint> mutations;
    private int page;
    private Button previous;
    private Button next;
    private Button back;
    private String selectedSpecies;

    private SeedJournalScreen(ItemStack journal) {
        super(Component.translatable("screen.cropgenetics.journal"));
        this.discovered = SeedJournalItem.discovered(journal);
        this.mutations = SeedJournalItem.knownMutations(journal);
    }

    public static void open(ItemStack journal) {
        Minecraft.getInstance().setScreen(new SeedJournalScreen(journal.copy()));
    }

    private int discoveryPages() {
        return Math.max(1, (discovered.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
    }

    private int totalPages() {
        return GUIDE_PAGES + discoveryPages()
                + Math.max(1, (mutations.size() + MUTATIONS_PER_PAGE - 1) / MUTATIONS_PER_PAGE);
    }

    @Override
    protected void init() {
        super.init();
        int center = width / 2;
        int bottom = height / 2 + 84;
        previous = addRenderableWidget(Button.builder(Component.literal("<"),
                button -> changePage(-1)).bounds(center - 93, bottom, 40, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"),
                button -> changePage(1)).bounds(center + 53, bottom, 40, 20).build());
        back = addRenderableWidget(Button.builder(
                Component.translatable("screen.cropgenetics.journal_back"),
                button -> { selectedSpecies = null; updateButtons(); })
                .bounds(center - 44, bottom, 88, 20).build());
        updateButtons();
    }

    private void changePage(int direction) {
        selectedSpecies = null;
        page = Math.max(0, Math.min(totalPages() - 1, page + direction));
        updateButtons();
    }

    private void updateButtons() {
        if (previous != null) previous.active = page > 0;
        if (next != null) next.active = page + 1 < totalPages();
        if (back != null) back.visible = selectedSpecies != null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = width / 2 - 112;
        int top = height / 2 - 101;
        graphics.fill(left, top, left + 224, top + 206, 0xFF3A2415);
        graphics.fill(left + 5, top + 5, left + 219, top + 201, 0xFFE6D4A8);
        if (page < GUIDE_PAGES) {
            renderGuidePage(graphics, left, top);
        } else if (page < GUIDE_PAGES + discoveryPages()) {
            renderDiscoveredPage(graphics, left, top);
        } else {
            renderMutationPage(graphics, left, top);
        }
        graphics.drawCenteredString(font, Component.literal((page + 1) + " / " + totalPages()),
                width / 2, top + 166, 0x694B30);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderGuidePage(GuiGraphics graphics, int left, int top) {
        String heading = switch (page) {
            case 0 -> "screen.cropgenetics.journal_intro";
            case 1 -> "screen.cropgenetics.journal_genetics";
            default -> "screen.cropgenetics.journal_crossing";
        };
        graphics.drawCenteredString(font, Component.translatable(heading), width / 2,
                top + 18, 0x54351C);
        for (int line = 0; line < 6; line++) {
            graphics.drawString(font, Component.translatable(
                    "screen.cropgenetics.guide_" + page + "_" + line),
                    left + 15, top + 45 + line * 19, 0x49301C, false);
        }
    }

    private void renderDiscoveredPage(GuiGraphics graphics, int left, int top) {
        if (selectedSpecies != null) {
            renderPlantDetail(graphics, left, top, selectedSpecies);
            return;
        }
        graphics.drawCenteredString(font, title, width / 2, top + 15, 0x54351C);
        graphics.drawCenteredString(font,
                Component.translatable("screen.cropgenetics.journal_count", discovered.size()),
                width / 2, top + 32, 0x694B30);
        if (discovered.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.cropgenetics.journal_empty"),
                    width / 2, top + 80, 0x674A31);
            return;
        }
        int start = (page - GUIDE_PAGES) * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, discovered.size());
        for (int i = start; i < end; i++) {
            String species = discovered.get(i);
            int y = top + 48 + (i - start) * 17;
            graphics.renderItem(iconFor(species), left + 17, y - 4);
            graphics.drawString(font, CropSpeciesNames.label(species),
                    left + 39, y, 0x49301C, false);
        }
    }

    private void renderMutationPage(GuiGraphics graphics, int left, int top) {
        graphics.drawCenteredString(font,
                Component.translatable("screen.cropgenetics.journal_mutations"),
                width / 2, top + 15, 0x54351C);
        if (mutations.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.cropgenetics.journal_no_mutations"),
                    width / 2, top + 70, 0x674A31);
            return;
        }
        int mutationPage = page - GUIDE_PAGES - discoveryPages();
        int start = mutationPage * MUTATIONS_PER_PAGE;
        int end = Math.min(start + MUTATIONS_PER_PAGE, mutations.size());
        for (int i = start; i < end; i++) {
            SeedJournalItem.MutationHint hint = mutations.get(i);
            int y = top + 37 + (i - start) * 41;
            graphics.drawString(font, CropSpeciesNames.label(hint.parentA()),
                    left + 17, y, 0x49301C, false);
            graphics.drawString(font, Component.literal("+").append(
                    CropSpeciesNames.label(hint.parentB())),
                    left + 17, y + 12, 0x49301C, false);
            graphics.drawString(font, Component.literal("→ ").append(
                    CropSpeciesNames.label(hint.child())),
                    left + 17, y + 24, 0x34502A, false);
        }
        graphics.drawString(font,
                Component.translatable("screen.cropgenetics.journal_conditions"),
                left + 15, top + 154, 0x694B30, false);
    }

    /** Click a discovered plant to open its species page. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && selectedSpecies == null && page >= GUIDE_PAGES
                && page < GUIDE_PAGES + discoveryPages()) {
            int left = width / 2 - 112;
            int top = height / 2 - 101;
            int first = (page - GUIDE_PAGES) * ENTRIES_PER_PAGE;
            if (mouseX >= left + 15 && mouseX < left + 209) {
                for (int row = 0; row < ENTRIES_PER_PAGE && first + row < discovered.size(); row++) {
                    int y = top + 48 + row * 17;
                    if (mouseY >= y - 4 && mouseY < y + 13) {
                        selectedSpecies = discovered.get(first + row);
                        updateButtons();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderPlantDetail(GuiGraphics graphics, int left, int top, String species) {
        graphics.drawCenteredString(font,
                Component.translatable("screen.cropgenetics.journal_plant_detail"),
                width / 2, top + 18, 0x54351C);
        graphics.renderItem(iconFor(species), left + 17, top + 43);
        graphics.drawString(font, CropSpeciesNames.label(species),
                left + 40, top + 47, 0x49301C, false);
        graphics.drawString(font, Component.translatable("screen.cropgenetics.journal_unlocked"),
                left + 17, top + 76, 0x49301C, false);
        graphics.drawString(font, Component.translatable("screen.cropgenetics.journal_seed_values"),
                left + 17, top + 100, 0x49301C, false);
        graphics.drawString(font, Component.translatable("screen.cropgenetics.journal_analyzer_values"),
                left + 17, top + 118, 0x49301C, false);

        // Research pages show only recipes whose complete parent/child
        // combination has already been unlocked by this very journal.
        List<SeedJournalItem.MutationHint> related = mutations.stream()
                .filter(hint -> species.equals(hint.parentA())
                        || species.equals(hint.parentB())
                        || species.equals(hint.child()))
                .toList();
        graphics.drawString(font,
                Component.translatable("screen.cropgenetics.journal_related_mutations", related.size()),
                left + 17, top + 139, 0x49301C, false);
        if (!related.isEmpty()) {
            graphics.drawString(font,
                    Component.literal("→ ").append(CropSpeciesNames.label(related.get(0).child())),
                    left + 17, top + 155, 0x34502A, false);
        }
    }

    /** Prefer a familiar Vanilla seed icon; unknown datapack species use our seed. */
    private static ItemStack iconFor(String species) {
        String source = SeedSpeciesCatalog.vanillaSeedForSpecies(species);
        if (source != null) {
            ResourceLocation key = ResourceLocation.tryParse(source);
            if (key != null) {
                var item = BuiltInRegistries.ITEM.getOptional(key);
                if (item.isPresent()) return new ItemStack(item.get());
            }
        }
        ItemStack fallback = CropItems.GENETIC_SEED.get().getDefaultInstance();
        fallback.set(CropComponents.GENOME.get(), Genome.basic(species));
        return fallback;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
