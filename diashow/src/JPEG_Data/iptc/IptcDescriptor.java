/*
 * Created by dnoakes on 21-Nov-2002 17:58:19 using IntelliJ IDEA.
 */
package JPEG_Data.iptc;

import JPEG_Data.Directory;
import JPEG_Data.MetadataException;
import JPEG_Data.TagDescriptor;

/**
 *
 */
public class IptcDescriptor extends TagDescriptor
{
    public IptcDescriptor(Directory directory)
    {
        super(directory);
    }

    public String getDescription(int tagType) throws MetadataException
    {
        return _directory.getString(tagType);
    }
}
