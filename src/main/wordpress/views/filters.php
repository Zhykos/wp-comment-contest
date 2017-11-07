<?php

/*  Copyright 2009 - 2015 Comment Contest plug-in for Wordpress by Thomas "Zhykos" Cicognani  (email : tcicognani@zhyweb.org)

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

?>

<div style="clear:both;">
    <span>
        <a href="javascript:;" onclick="toggleFilters('<?php echo $this->pluginDir; ?>')"><img src="<?php echo $this->pluginDir; ?>/img/plus.png" alt="expand/collapse" id="filtersImg" style="vertical-align: middle;" /></a>
        <a href="javascript:;" onclick="toggleFilters('<?php echo $this->pluginDir; ?>')"><?php _e('Filters', "comment-contest"); ?></a>
    </span>
    <div id="filters" style="display: none;">
        <h4><?php _e('Contest deadline', "comment-contest"); ?></h4>
        <div id="zwpcc_dateFilter_error_message" style="color: red; display: none;"><?php _e('Wrong date format!', "comment-contest"); ?></div>
        <?php _e('Date:', "comment-contest"); ?> <input type="text" id="datepicker" />
        <br /><?php _e('Hour (24h format):', "comment-contest"); ?> <input type="text" id="dateHours" maxlength="2" size="3" /><?php _e('h', "comment-contest"); ?> <input type="text" id="dateMinutes" maxlength="2" size="3" /><?php _e('min', "comment-contest"); ?><br />
        <br /><input type="button" class="button action" id="dateSubmit" value="<?php _e('Select comments after this deadline', "comment-contest"); ?>" />

        <br /><br />
        <h4><?php _e('Name', "comment-contest"); echo "<img src=\"$this->pluginDir/img/help.png\" alt=\"Help\" class=\"help\" title=\"". __("You can allow people with the same alias to post several comments for the contest. This filter limits the maximum number of comments for the same person.<br />(ex. 0 means a person is not allowed to post more than one time; 2 means only two comments from the same person will be kept for the contest)", "comment-contest") . "\" />"; ?></h4>
        <div id="zwpcc_aliasFilter_error_message" style="color: red; display: none;"><?php _e('Wrong configuration! This number must be equals or greater than zero', "comment-contest"); ?></div>
        <?php _e('Same name maximum use:', "comment-contest"); ?> <input type="text" id="aliasConfig" maxlength="2" size="3" value="1" />
        <br /><input type="button" class="button action" id="aliasAddressFilter" value="<?php _e('Select comments with the same name', "comment-contest"); ?>" />

        <br /><br />
        <h4><?php _e('Email address', "comment-contest"); echo "<img src=\"$this->pluginDir/img/help.png\" alt=\"Help\" class=\"help\" title=\"". __("You can allow people with the same email to post several comments for the contest. This filter limits the maximum number of comments for the same email.<br />(ex. 0 means an email is not allowed to post more than one time; 2 means only two comments from the same email will be kept for the contest)", "comment-contest") . "\" />"; ?></h4>
        <div id="zwpcc_emailFilter_error_message" style="color: red; display: none;"><?php _e('Wrong configuration! This number must be equals or greater than zero', "comment-contest"); ?></div>
        <?php _e('Same email maximum use:', "comment-contest"); ?> <input type="text" id="emailConfig" maxlength="2" size="3" value="1" />
        <br /><input type="button" class="button action" id="emailAddressFilter" value="<?php _e('Select comments with the same email', "comment-contest"); ?>" />
        
        
        <br /><br />
        <h4><?php _e('IP address', "comment-contest"); echo "<img src=\"$this->pluginDir/img/help.png\" alt=\"Help\" class=\"help\" title=\"". __("You can allow people with the same IP address to post several comments for the contest. This filter limits the maximum number of comments for the same IP address.<br />(ex. 0 means a IP address is not allowed to post more than one time; 2 means only two comments from the same IP address will be kept for the contest)", "comment-contest") . "\" />"; ?></h4>
        <div id="zwpcc_ipFilter_error_message" style="color: red; display: none;"><?php _e('Wrong configuration! This number must be equals or greater than zero', "comment-contest"); ?></div>
        <?php _e('Same IP address maximum use:', "comment-contest"); ?> <input type="text" id="ipConfig" maxlength="2" size="3" value="1" />
        <br /><input type="button" class="button action" id="ipAddressFilter" value="<?php _e('Select comments with the same IP address', "comment-contest"); ?>" />

        <br /><br />
        <h4><?php _e('Comment', "comment-contest"); ?></h4>
        <?php _e('Words (comma separated):', "comment-contest"); ?> <input type="text" id="words" size="30" />
        <br /><input type="button" class="button action" id="wordsFilter" value="<?php _e('Select comments which don\'t contain one of these words', "comment-contest"); ?>" />
        <br /><input type="button" class="button action" id="allWordsFilter" value="<?php _e('Select comments which don\'t contain all these words', "comment-contest"); ?>" />
        
        <br /><br />
        <h4><?php _e('Time between two comments', "comment-contest"); echo "<img src=\"$this->pluginDir/img/help.png\" alt=\"Help\" class=\"help\" title=\"". __("You can allow people to post several comments in your contest. Between two comments the person must wait some times. This filter allow you to get all comments (from a person who has the same name/email/IP address) which don't respect your configuration", "comment-contest") . "\" />"; ?></h4>
        <div id="zwpcc_timeBetweenFilter_error_message" style="color: red; display: none;"><?php _e('Wrong configuration! This number must be greater than zero and at least one criterion must be checked!', "comment-contest"); ?></div>
        <?php _e('Time (in minutes [1 day = 1440 min]):', "comment-contest"); ?> <input type="text" id="timeBetween" size="10" maxlength="6" />
        <br /><?php _e('Criteria:', "comment-contest"); ?>
        <input type="checkbox" id="timeBetweenFilterName" checked="checked" /><label for="timeBetweenFilterName"><?php _e('Name', "comment-contest"); ?></label>
         / <input type="checkbox" id="timeBetweenFilterEmail" /><label for="timeBetweenFilterEmail"><?php _e('Email', "comment-contest"); ?></label>
         / <input type="checkbox" id="timeBetweenFilterIP" /><label for="timeBetweenFilterIP"><?php _e('IP address', "comment-contest"); ?></label>
        <br /><input type="button" class="button action" id="timeBetweenFilter" value="<?php _e('Select comments posted too soon', "comment-contest"); ?>" />
    </div>
</div>