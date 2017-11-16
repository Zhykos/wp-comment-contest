package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class CommentContestPlugin implements IWordPressPlugin {

	@Override
	public String getName() {
		return "Comment Contest"; //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return "comment-contest"; //$NON-NLS-1$
	}

	@Override
	public void defaultActivationAction() {
		// TODO Auto-generated method stub
checkmessage
	}

}
