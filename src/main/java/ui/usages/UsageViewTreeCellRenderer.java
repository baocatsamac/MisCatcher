package ui.usages;

import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.DarculaColors;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.usageView.UsageTreeColors;
import com.intellij.usageView.UsageTreeColorsScheme;
import com.intellij.usageView.UsageViewBundle;
import com.intellij.usages.*;
import com.intellij.util.FontUtil;
import com.intellij.util.ui.UIUtil;
import helper.PermissionHelper;
import object.Method;
import object.UIElement;
import org.jetbrains.annotations.NotNull;
import service.Holder;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class UsageViewTreeCellRenderer extends ColoredTreeCellRenderer {
    private static final Logger LOG = Logger.getInstance("#ui.usages.UsageViewTreeCellRenderer");
    private static final EditorColorsScheme ourColorsScheme = UsageTreeColorsScheme.getInstance().getScheme();
    private static final SimpleTextAttributes ourInvalidAttributes = SimpleTextAttributes.fromTextAttributes(ourColorsScheme.getAttributes(UsageTreeColors.INVALID_PREFIX));
    private static final SimpleTextAttributes ourReadOnlyAttributes = SimpleTextAttributes.fromTextAttributes(ourColorsScheme.getAttributes(UsageTreeColors.READONLY_PREFIX));
    private static final SimpleTextAttributes ourNumberOfUsagesAttribute = SimpleTextAttributes.fromTextAttributes(ourColorsScheme.getAttributes(UsageTreeColors.NUMBER_OF_USAGES));
    private static final SimpleTextAttributes ourInvalidAttributesDarcula = new SimpleTextAttributes(null, DarculaColors.RED, null, ourInvalidAttributes.getStyle());
    private static final Insets STANDARD_IPAD_NOWIFI = new Insets(1, 2, 1, 2);
    private boolean myRowBoundsCalled;

    private final UsageViewPresentation myPresentation;
    private final UsageView myView;
    private boolean myCalculated;
    private int myRowHeight = AllIcons.Nodes.AbstractClass.getIconHeight() + 2;

    UsageViewTreeCellRenderer(@NotNull UsageView view) {
        myView = view;
        myPresentation = view.getPresentation();
    }

    private Dimension cachedPreferredSize;

    @NotNull
    @Override
    public Dimension getPreferredSize() {
        return myCalculated ? super.getPreferredSize() : new Dimension(10, myRowHeight);
    }

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        boolean showAsReadOnly = false;
        if (value instanceof Node && value != tree.getModel().getRoot()) {
            Node node = (Node) value;
            if (!node.isValid()) {
                append(UsageViewBundle.message("node.invalid") + " ", UIUtil.isUnderDarcula() ? ourInvalidAttributesDarcula : ourInvalidAttributes);
            }
            if (myPresentation.isShowReadOnlyStatusAsRed() && node.isReadOnly()) {
                showAsReadOnly = true;
            }
        }

        myCalculated = false;
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            Object userObject = treeNode.getUserObject();

            Rectangle visibleRect = ((JViewport) tree.getParent()).getViewRect();
            if (!visibleRect.isEmpty()) {
                //Protection against SOE on some OSes and JDKs IDEA-120631
                UsageViewTreeCellRenderer.RowLocation visible = myRowBoundsCalled ? UsageViewTreeCellRenderer.RowLocation.INSIDE_VISIBLE_RECT : isRowVisible(row, visibleRect);
                myRowBoundsCalled = false;
                if (visible != UsageViewTreeCellRenderer.RowLocation.INSIDE_VISIBLE_RECT) {
                    // for the node outside visible rect do not compute (expensive) presentation
                    return;
                }
                if (!getIpad().equals(STANDARD_IPAD_NOWIFI)) {
                    // for the visible node, return its ipad to the standard value
                    setIpad(STANDARD_IPAD_NOWIFI);
                }
            }

            // we can be called recursively via isRowVisible()
            if (myCalculated) return;
            myCalculated = true;

            if (userObject instanceof UsageTarget) {
                UsageTarget usageTarget = (UsageTarget) userObject;
                if (!usageTarget.isValid()) {
                    if (!getCharSequence(false).toString().contains(UsageViewBundle.message("node.invalid"))) {
                        append(UsageViewBundle.message("node.invalid"), ourInvalidAttributes);
                    }
                    return;
                }

                final ItemPresentation presentation = usageTarget.getPresentation();
                LOG.assertTrue(presentation != null);
                if (showAsReadOnly) {
                    append(UsageViewBundle.message("node.readonly") + " ", ourReadOnlyAttributes);
                }
                final String text = presentation.getPresentableText();
                append(text == null ? "" : text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                setIcon(presentation.getIcon(expanded));
            } else if (treeNode instanceof GroupNode) {
                GroupNode node = (GroupNode) treeNode;

                if (node.isRoot()) {
                    append(StringUtil.capitalize(myPresentation.getUsagesWord()), patchAttrs(node, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES));
                } else {
                    append(node.getGroup().getText(myView),
                            patchAttrs(node, showAsReadOnly ? ourReadOnlyAttributes : SimpleTextAttributes.REGULAR_ATTRIBUTES));
                    setIcon(node.getGroup().getIcon(expanded));
                }

                int count = node.getRecursiveUsageCount();
                SimpleTextAttributes attributes = patchAttrs(node, ourNumberOfUsagesAttribute);

                append(FontUtil.spaceAndThinSpace() + StringUtil.pluralize(count + " " + myPresentation.getUsagesWord(), count),
                        SimpleTextAttributes.GRAYED_ATTRIBUTES.derive(attributes.getStyle(), null, null, null));
            } else if (treeNode instanceof UsageNode) {
                UsageNode node = (UsageNode) treeNode;

                setIcon(node.getUsage().getPresentation().getIcon());
                if (showAsReadOnly) {
                    append(UsageViewBundle.message("node.readonly") + " ", patchAttrs(node, ourReadOnlyAttributes));
                }

                if (node.isValid()) {
                    TextChunk[] text = node.getUsage().getPresentation().getText();
                    PsiElement psiElement = node.getPsiElement();
                    String signature = null;
//                    boolean isCryptoMisuse = false;
                    for (int i = 0; i < text.length; i++) {
                        TextChunk textChunk = text[i];
                        SimpleTextAttributes simples = textChunk.getSimpleAttributesIgnoreBackground();
//                        if (isCryptoMisuse) {
//                            simples = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, JBColor.RED);
//                        }

                        append(textChunk.getText() + (i == 0 ? " " : ""), patchAttrs(node, simples), true);

                    }

                    if (psiElement instanceof PsiMethodCallExpression) {
                        ArrayList<String> usedPermissons = getUsedPermissions(psiElement);
                        if (usedPermissons != null) {
                            append(" > use permission: ");
                            for (String perm : usedPermissons) {
                                append(perm.split("\\.")[2] + " ");
                            }
                        }

                    } else if (psiElement instanceof XmlTag) {
                        ArrayList<String> advertisedPermissons = getAdvertisedPermissions(psiElement);
                        if (advertisedPermissons != null) {
                            String textMessage = getMessage(psiElement);
                            if (textMessage.isEmpty()) {
                                append(String.format(" > this %s and its associated layout can infer permissions: ", ((XmlTag) psiElement).getName().toLowerCase()));
                            } else {
                                append(String.format(" > this %s with %s can infer permissions: ", ((XmlTag) psiElement).getName().toLowerCase(), textMessage));
                            }

                            for (String perm : advertisedPermissons) {
                                append(perm.split("\\.")[2] + " ");
                            }
                        }
                    }


                }
            } else if (userObject instanceof String) {
                append((String) userObject, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            } else {
                append(userObject == null ? "" : userObject.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        } else {
            append(value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        SpeedSearchUtil.applySpeedSearchHighlighting(tree, this, true, mySelected);
    }

    private String getMessage(PsiElement psiElement) {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> textData = null;
        for (Map.Entry<XmlTag, ArrayList<String>> map : Holder.getTagKeywordsMapping().entrySet()) {
            if (map.getKey().equals(psiElement)) {
                textData = map.getValue();
                break;
            }
        }

        if (textData != null) {
            stringBuilder.append("text as ");
            for (int i = 0; i < textData.size(); i++) {
                if (i != textData.size() - 1) {
                    stringBuilder.append(textData.get(i) + " and ");
                } else {
                    stringBuilder.append(textData.get(i));
                }

            }
        }
        return stringBuilder.toString();
    }


    private ArrayList<String> getUsedPermissions(PsiElement psiElement) {
        for (UIElement uiElement : Holder.getUiElements()) {
            if (uiElement.getCodeFile().equals(psiElement.getContainingFile())) {
                for (Method method : uiElement.getUiInteractiveMethods()) {
                    if (method.getCodeLinePermissionMappings().keySet().contains(psiElement)) {
                        return method.getCodeLinePermissionMappings().get(psiElement);
                    }
                }
            }
        }
        return null;
    }

    private ArrayList<String> getAdvertisedPermissions(PsiElement psiElement) {
        if (Holder.getTagKeywordsMapping().containsKey(psiElement)) {
            return PermissionHelper.inferPermissionsFromText(Holder.getTagKeywordsMapping().get(psiElement));
        }
        return null;

    }

    // computes the node text regardless of the node visibility
    @NotNull
    String getPlainTextForNode(Object value) {
        boolean showAsReadOnly = false;
        StringBuilder result = new StringBuilder();
        if (value instanceof Node) {
            Node node = (Node) value;
            if (!node.isValid()) {
                result.append(UsageViewBundle.message("node.invalid")).append(" ");
            }
            if (myPresentation.isShowReadOnlyStatusAsRed() && node.isReadOnly()) {
                showAsReadOnly = true;
            }
        }

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            Object userObject = treeNode.getUserObject();

            if (userObject instanceof UsageTarget) {
                UsageTarget usageTarget = (UsageTarget) userObject;
                if (usageTarget.isValid()) {
                    final ItemPresentation presentation = usageTarget.getPresentation();
                    LOG.assertTrue(presentation != null);
                    if (showAsReadOnly) {
                        result.append(UsageViewBundle.message("node.readonly")).append(" ");
                    }
                    final String text = presentation.getPresentableText();
                    result.append(text == null ? "" : text);
                } else {
                    result.append(UsageViewBundle.message("node.invalid"));
                }
            } else if (treeNode instanceof GroupNode) {
                GroupNode node = (GroupNode) treeNode;

                if (node.isRoot()) {
                    result.append(StringUtil.capitalize(myPresentation.getUsagesWord()));
                } else {
                    result.append(node.getGroup().getText(myView));
                }

                int count = node.getRecursiveUsageCount();
                result.append(" (").append(StringUtil.pluralize(count + " " + myPresentation.getUsagesWord(), count)).append(")");
            } else if (treeNode instanceof UsageNode) {
                UsageNode node = (UsageNode) treeNode;

                if (showAsReadOnly) {
                    result.append(UsageViewBundle.message("node.readonly")).append(" ");
                }

                if (node.isValid()) {
                    TextChunk[] text = node.getUsage().getPresentation().getText();
                    for (TextChunk textChunk : text) {
                        result.append(textChunk.getText());
                    }
                }
            } else if (userObject instanceof String) {
                result.append((String) userObject);
            } else {
                result.append(userObject == null ? "" : userObject.toString());
            }
        } else {
            result.append(value);
        }
        return result.toString();
    }

    enum RowLocation {
        BEFORE_VISIBLE_RECT, INSIDE_VISIBLE_RECT, AFTER_VISIBLE_RECT
    }

    @NotNull
    RowLocation isRowVisible(int row, @NotNull Rectangle visibleRect) {
        Dimension pref;
        if (cachedPreferredSize == null) {
            cachedPreferredSize = pref = getPreferredSize();
        } else {
            pref = cachedPreferredSize;
        }
        pref.width = Math.max(visibleRect.width, pref.width);
        myRowBoundsCalled = true;
        JTree tree = getTree();
        final Rectangle bounds = tree == null ? null : tree.getRowBounds(row);
        myRowBoundsCalled = false;
        if (bounds != null) {
            myRowHeight = bounds.height;
        }
        int y = bounds == null ? 0 : bounds.y;
        TextRange vis = TextRange.from(Math.max(0, visibleRect.y - pref.height), visibleRect.height + pref.height * 2);
        boolean inside = vis.contains(y);
        if (inside) {
            return UsageViewTreeCellRenderer.RowLocation.INSIDE_VISIBLE_RECT;
        }
        return y < vis.getStartOffset() ? UsageViewTreeCellRenderer.RowLocation.BEFORE_VISIBLE_RECT : UsageViewTreeCellRenderer.RowLocation.AFTER_VISIBLE_RECT;
    }


    private static SimpleTextAttributes patchAttrs(@NotNull Node node, @NotNull SimpleTextAttributes original) {

        if (node.isExcluded()) {
            original = new SimpleTextAttributes(original.getStyle() | SimpleTextAttributes.STYLE_STRIKEOUT, original.getFgColor(), original.getWaveColor());
        }
        if (node instanceof GroupNode) {
            UsageGroup group = ((GroupNode) node).getGroup();
            FileStatus fileStatus = group != null ? group.getFileStatus() : null;
            if (fileStatus != null && fileStatus != FileStatus.NOT_CHANGED) {
                original = new SimpleTextAttributes(original.getStyle(), fileStatus.getColor(), original.getWaveColor());
            }

            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            if (parent != null && parent.isRoot()) {

                original = new SimpleTextAttributes(original.getStyle() | SimpleTextAttributes.STYLE_BOLD, original.getFgColor(), original.getWaveColor());
            }
        }
        return original;
    }

    static String getTooltipFromPresentation(final Object value) {
        String tooltip = null;
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            if (treeNode instanceof UsageNode) {

                UsageNode node = (UsageNode) treeNode;
                PsiElement psiElement = node.getPsiElement();
                tooltip = node.getUsage().getPresentation().getTooltipText();
//                if (psiElement != null) {
//                    String signature = LibraryDependenciesBuilder.getMethodSignature(psiElement);
//                    if (signature != null) {
//                        LibraryDependenciesBuilder dependenciesBuilder = LibraryDependenciesBuilder.getInstance(psiElement.getProject());
//
//                        HashMap<String, CryptoIssue> usedAPIs = dependenciesBuilder.getUsedAPIs();
//                        CryptoIssue issue = usedAPIs.get(signature);
//                        if (issue != null) {
//                            tooltip = "Location: " + issue.getMethod() +
//                                    "\nBroken Rule:" + issue.getRuleName() +
//                                    "\nDetails: " + issue.getDetails();
//                        }
//
//                    } else {
//
//                        tooltip = node.getUsage().getPresentation().getTooltipText();
//                    }
//                } else {
//                    tooltip = node.getUsage().getPresentation().getTooltipText();
//                }
            }
        }
        return tooltip;
    }
}
