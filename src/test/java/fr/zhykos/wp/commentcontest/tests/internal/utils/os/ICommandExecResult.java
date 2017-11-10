package fr.zhykos.wp.commentcontest.tests.internal.utils.os;

public interface ICommandExecResult {

	int getExitValue();

	String getOuput();

	String getErrorOutput();

}
