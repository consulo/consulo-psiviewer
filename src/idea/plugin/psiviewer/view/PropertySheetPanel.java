/*
 *  Copyright (c) 2002 Sabre, Inc. All rights reserved.
 */
package idea.plugin.psiviewer.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.xml.util.XmlStringUtil;
import idea.plugin.psiviewer.util.IntrospectionUtil;

/**
 * A property sheet describing the selected PSI element.
 *
 * @author <a href="mailto:jacmorel@yahoo.com">Jacques Morel</a>
 */
public class PropertySheetPanel extends JPanel
{
    private Object _target;
    private JTable myTable;

    private static final Logger LOG = Logger.getInstance("idea.plugin.psiviewer.view.PropertySheetPanel");

    public PropertySheetPanel()
    {
        super(new BorderLayout());
    }

    public void setTarget(Object bean)
    {
        debug("setTarget=" + bean);
        setVisible(false);
        removeAll();

        _target = bean;
        if (_target == null) return;

        List<PropertyDescriptor> properties = getReadProperties();

        Object[][] tableData = new Object[properties.size()][2];
        Object[] columnTitles = new String[]{"Property", "Value"};

        Map<Object,String> map = new TreeMap<Object, String>(); // Guarantees ascending natural key sort order
        for (PropertyDescriptor property : properties)
        {
            String key = property.getDisplayName();
            String value = formattedToString(IntrospectionUtil.getValue(_target, property));

            if(StringUtil.isNotEmpty(value) && StringUtil.startsWithIgnoreCase(value, "<html>"))
            {
                value = "<html>" + XmlStringUtil.escapeString(value) + "</html>";
            }

            map.put(key, value);
        }

        int i = 0;
        for (Iterator<Map.Entry<Object,String>> it = map.entrySet().iterator(); it.hasNext(); i++)
        {
            Map.Entry<Object,String> entry = it.next();
            Object[] rowData = tableData[i];
            rowData[0] = entry.getKey();
            rowData[1] = entry.getValue();
        }

        add(createTable(tableData, columnTitles));
        setVisible(true);
    }

    public JTable getTable()
    {
        return myTable;
    }

    private JScrollPane createTable(Object[][] tableData, Object[] columnTitle)
    {
        myTable = new JTable(tableData, columnTitle)
        {
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }

            public JToolTip createToolTip()
            {
                PropertySheetToolTip.getInstance().setComponent(this);
                return PropertySheetToolTip.getInstance();
            }

            public String getToolTipText(MouseEvent event)
            {
                int col = columnAtPoint(event.getPoint());
                int row = rowAtPoint(event.getPoint());

                String tip = (String) getValueAt(row, col);

                Graphics2D g2 = (Graphics2D) getGraphics();
                Rectangle2D tipRect = getFont().getStringBounds(tip, g2.getFontRenderContext());
                g2.dispose();

                Rectangle visibleCell = getVisibleRect().intersection(getCellRect(row, col, false));

                if (tipRect.getWidth() + 1 < visibleCell.getWidth())
                    tip = null;   // Cell content is completely visible, so no tip is required
                return tip;
            }

            private static final boolean INCLUDE_INTERCELL_SPACING = true;

            public Point getToolTipLocation(MouseEvent event)
            {
                int col = columnAtPoint(event.getPoint());
                int row = rowAtPoint(event.getPoint());
                return getCellRect(row, col, INCLUDE_INTERCELL_SPACING).getLocation();
            }


        };

        myTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        myTable.getSelectionModel().setSelectionMode(0);

        packColumn(myTable, 0, 2);
        packColumn(myTable, 1, 2);

        return ScrollPaneFactory.createScrollPane(myTable, true);
    }

    private void packColumn(JTable table, int colIndex, int margin)
    {
        int width = Math.max(getColumnHeaderWidth(table, colIndex),
                             getColumnCellWidth(table, colIndex));

        width += 2 * margin;

        TableColumn column = table.getColumnModel().getColumn(colIndex);
        column.setPreferredWidth(width);
        column.setMinWidth(colIndex==0?width:0);
        column.setMaxWidth(colIndex==0?width:Integer.MAX_VALUE);
    }

    private int getColumnCellWidth(JTable table, int colIndex)
    {
        int width = 0;
        for (int r = 0; r < table.getRowCount(); r++)
        {
            TableCellRenderer renderer = table.getCellRenderer(r, colIndex);
            Component comp = renderer.getTableCellRendererComponent(
                table, table.getValueAt(r, colIndex), false, false, r, colIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }
        return width;
    }

    private int getColumnHeaderWidth(JTable table, int colIndex)
    {
        TableColumn col = table.getColumnModel().getColumn(colIndex);
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null)
        {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(
            table, col.getHeaderValue(), false, false, 0, 0);
        return comp.getPreferredSize().width;
    }

    private static String formattedToString(Object object)
    {
        if (object == null) return "null";
        if (!object.getClass().isArray()) return object.toString();
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        Object[] array = (Object[]) object;
        for (int i = 0; i < array.length; i++) // fixme what if length is 100_500_000 ?
        {
            if (i != 0) buf.append(", ");
            buf.append(array[i] == null ? "null" : array[i].toString());
        }
        buf.append("]");
        return buf.toString();
    }

    private List<PropertyDescriptor> getReadProperties()
    {
        PropertyDescriptor[] properties = IntrospectionUtil.getProperties(_target.getClass());
        List<PropertyDescriptor> readProperties;
        readProperties = new ArrayList<PropertyDescriptor>(properties.length);
        for (PropertyDescriptor property : properties)
        {
            if (property.getReadMethod() != null)
            {
                 readProperties.add(property);
            }
        }
        return readProperties;
    }

    private static void debug(String message)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug(message);
        }
    }
}