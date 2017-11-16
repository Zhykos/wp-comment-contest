package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class WPRSSAggregatorPlugin implements IWordPressPlugin {

	@Override
	public String getName() {
		return "WP RSS Aggregator"; //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return "wp-rss-aggregator"; //$NON-NLS-1$
	}

	@Override
	public void defaultActivationAction() {
		// TODO Auto-generated method stub
checkmessage et peut être faire un truc en plus
	}

}
