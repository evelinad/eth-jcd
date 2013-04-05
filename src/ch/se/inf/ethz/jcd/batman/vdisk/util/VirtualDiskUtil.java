package ch.se.inf.ethz.jcd.batman.vdisk.util;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDirectory;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskEntry;

public class VirtualDiskUtil {

    public static Collection<IVirtualDiskEntry> getDirectoryMembers(
            IVirtualDirectory directory) throws IOException {

        List<IVirtualDiskEntry> members = new LinkedList<IVirtualDiskEntry>();
        IVirtualDiskEntry firstMember = directory.getFirstMember();
        if (firstMember != null) {
            for (IVirtualDiskEntry currentMember : firstMember) {
                members.add(currentMember);
            }
        }

        return members;
    }

    public static IVirtualDiskEntry getDirectoryMember(
            IVirtualDirectory directory, String name) throws IOException {

        IVirtualDiskEntry firstChild = directory.getFirstMember();

        if (firstChild == null) {
            return null;
        } else {
            for (IVirtualDiskEntry currentMember : firstChild) {
                if (currentMember.getName().equals(name)) {
                    return currentMember;
                }
            }

            return null;
        }
    }

    public static Collection<String> getDirectoryMemberNames(
            IVirtualDirectory directory) throws IOException {

        Collection<String> names = new LinkedList<String>();
        IVirtualDiskEntry firstChild = directory.getFirstMember();

        if (firstChild != null) {
            for (IVirtualDiskEntry entry : directory.getFirstMember()) {
                names.add(entry.getName());
            }
        }

        return names;
    }

}
