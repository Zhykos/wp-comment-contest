=== Plugin Name ===
Contributors: zhykos
Donate link: http://www.nozzhy.com
Tags: comments contest nozzhy nozgarde zhykos concours commentaire
Requires at least: 2.8.4
Tested up to: 2.9.2
Stable tag: trunk

If you create a contest on your website, you can draw all comments in a specific post.

== Description ==

If you want to organize a contest on your blog (some goodies, games ... to win), use this plugin. Comment Contest works only with comments.
You choose some comments, set some features (winners' number ...) and the system chooses the winners.

== Installation ==

This section describes how to install the plugin and get it working.

1. Upload directory `comment-contest` to the `/wp-content/plugins/` directory
2. Activate the plugin through the 'Plugins' menu in WordPress
3. Go to 'Comment Contest' menu in 'Plugins' menu

== Frequently Asked Questions ==

= Which PHP version I need? =

You need PHP version 4 like Wordpress. The plugin is ready to run with PHP5 but it isn't activated.

== Screenshots ==
1. The first page of the plugin : set the contest's features
2. Choose the article in which the contest is running
3. Choose the comments
4. Choose the prizes to win
5. The winners are displayed

== Changelog ==

= 1.0 =
* Release version
* Only French language is available

= 1.1 =
* Translation in English and French
* Comments are included in the source code
* Screenshots are available in the install

= 1.1.0.1 =
* Remove "plugin only in french" in a comment

= 1.1.1b =
* Send a mail to the winners at the end. You can choose to do so or not (not tested yet)

= 1.1.1 =
* Bug fix : send a mail to winners (tested online)
* Bug fix : can display other posts

= 1.1.2 =
* Add the possibility to choose between Normal Contest or Speed Contest. Speed Contest choose winners by sorting them by chronologic order

= 1.2 =
* Set the prices to win
* Change PHP version for the main class. I migrate from PHP5 to version 4 because some servers still use PHP4 and PHP4 is the Wordpress recommandation

= 1.3 =
* Change error message display (now it's the same message as Wordpress). Old values are put in the fields so the user don't have to type again the values
* New winners display

= 1.35 =
* Only display posts with comments
* BUG FIX : Simple and double quotes protected because if a pseudo contains quotes, some query bug (thanks to Kamel from www.yoocom.fr)
* BUG FIX : Change the place of a check value (bug if the value was null)
* Different names check for prizes and code optimization
* Remove case sensitive sort for displaying all the participants
* Change error message format in certain cases
* Change localization message in French

= 1.36 =
* If you use MySQL version 4.0, the plugin doesn't work because sub-querys are not compatible with MySQL 4.0. You have to use MySQL 4.1.xxx at least but some website provider don't allow us to choose our version. Sub-querys are now removed.

= 1.37 =
* Some little improvements

= 1.4 =
* Tests with Wordpress 2.9.2
* Add an editor to write the email
* Add a message to send to loosers
* Email subject can be changed
* Display winners and loosers emails at the end (in case automatic email fails for example)