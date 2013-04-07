import java.io.File;

import org.apache.commons.io.filefilter.AbstractFileFilter;

/**
 * bring them all!
 */
public class NoneFilteringFilter extends AbstractFileFilter {

	@Override
	public boolean accept(File dir, String name) {
		return true;
	}

}
