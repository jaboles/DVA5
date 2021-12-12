package jb.dvacommon.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.jdom.Attribute;
import org.swixml.Localizer;
import org.swixml.Parser;
import org.swixml.converters.ImageIconConverter;

import javax.swing.*;

public class SvgIconConverter extends ImageIconConverter
{
    public Object convert(Class aClass, Attribute attribute, Localizer localizer)
    {
        if (attribute != null) {
            if (Parser.LOCALIZED_ATTRIBUTES.contains(attribute.getName().toLowerCase()) && attribute.getAttributeType() == 1) {
                attribute.setValue(localizer.getString(attribute.getValue()));
            }

            String resource = attribute.getValue();
            if (resource.startsWith("svg:")) {
                resource = resource.substring(4);
                return new FlatSVGIcon(resource);
            }
        }

        return super.convert(aClass, attribute, localizer);
    }

    public Class convertsTo() {return ImageIcon.class;}
}
