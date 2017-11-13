package fr.zhykos.wp.commentcontest.tests.internal.utils.min;

import java.io.File;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

public interface IMinFactory {

	IMinFactory DEFAULT = new MinFactory();

	IMin createMinProcessor(File source) throws UtilsException;

}
