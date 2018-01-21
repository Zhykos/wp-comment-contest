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

/*
 * Selected comment won't be used for contest
 * @param commentID Comment ID
 */
function commentContestDelete(commentID) {
    jQuery("#comment-contest-" + commentID).removeClass("cheatComment");
    jQuery("#comment-contest-" + commentID).addClass("removedComment");
    jQuery("#restoreLink-" + commentID).show();
    jQuery("#deleteLink-" + commentID).hide();
    
    jQuery("#stopCheatLink-" + commentID).hide();
    jQuery("#cheatLink-" + commentID).show();
}

/*
 * Selected comment must be used for contest
 * @param commentID Comment ID
 */
function commentContestRestore(commentID) {
    jQuery("#comment-contest-" + commentID).removeClass("removedComment");
    jQuery("#deleteLink-" + commentID).show();
    jQuery("#restoreLink-" + commentID).hide();
}

/**
 * Selected comment will win (if winners number greater than cheating comments number)
 * @param commentID Comment ID
 */
function commentContestCheat(commentID) {
    jQuery("#comment-contest-" + commentID).removeClass("removedComment");
    jQuery("#comment-contest-" + commentID).addClass("cheatComment");
    jQuery("#stopCheatLink-" + commentID).show();
    jQuery("#cheatLink-" + commentID).hide();

    jQuery("#deleteLink-" + commentID).show();
    jQuery("#restoreLink-" + commentID).hide();
}

/**
 * Selected comment won't win anymore (except if it will be randomly choosen during the contest last step)
 * @param commentID Comment ID
 */
function commentContestStopCheat(commentID) {
    jQuery("#comment-contest-" + commentID).removeClass("cheatComment");
    jQuery("#stopCheatLink-" + commentID).hide();
    jQuery("#cheatLink-" + commentID).show();
}

/*
 * Select all comments in the table which have the role "roleID"
 */
function selectRole(roleID) {
    // Browse all table lines
    jQuery('#contestForm tr').each(function() {
        var line = jQuery(this);
        
        // Search for span tag with class "zhyweb_comment_contest_role" which is equal to "roleID"
        // A user can have several roles, separated by a pipe (|)
        var rolesStr = line.find('.zhyweb_comment_contest_role').text();
        var roles = rolesStr.split('|');

        if (jQuery.inArray(roleID, roles) >= 0 && rolesStr.length > 0) {
            // Check the box in lines
            line.find('input[id^="cb-select"]').each(function() {
                jQuery(this).attr("checked", true);
            });
        }
    });
}

/*
 * Shuffle an array
 * @param myArray Array to shuffle
 * @see http://sedition.com/perl/javascript-fy.html
 */
function fisherYates(myArray) {
    var i = myArray.length;
    if (i > 0) {
        while (--i) {
            var j = Math.floor( Math.random() * ( i + 1 ) );
            var tempi = myArray[i];
            var tempj = myArray[j];
            myArray[i] = tempj;
            myArray[j] = tempi;
        }
    }
}

/**
 * Open / Hide filters DIV component
 */
function toggleFilters(pluginURL) {
    var filtersDIV = jQuery('#filters');
    
    filtersDIV.fadeToggle("slow", "linear", function() {
        if (filtersDIV.is(':visible')) {
            jQuery('#filtersImg').attr('src', pluginURL + '/img/minus.png');
        } else {
            jQuery('#filtersImg').attr('src', pluginURL + '/img/plus.png');
        }
    });
}

// -----------------------------------------------------------------------------
// jQuery ready document

jQuery(document).ready(function() {
    
    function checkInputsOfWrongComments(testFunction, useName, useEmail, useIP, selectDuplicates, nbAuthEntries) {
        var duplicates = [];
        var line1nb = 0;

        jQuery('#contestForm tr').each(function() {
            // Browse all table lines
            var line1 = jQuery(this);
            var line1Name = null;
            if (useName) {
                line1Name = line1.find('.zhyweb_comment_contest_alias').text();
            }
            
            var line1Email = null;
            if (useEmail) {
                line1Email = line1.find('.zhyweb_comment_contest_email').text();
            }
            
            var line1IP = null;
            if (useIP) {
                line1IP = line1.find('.zhyweb_comment_contest_ip').text();
            }

            if (line1Name || line1Email || line1IP) { // With this condition, we are sure we are not on header/footer line of the table
                var line2nb = 0;
                // Browse all table lines
                jQuery('#contestForm tr').each(function() {
                    // Compare two different lines and do not compare previously compared lines
                    if (line2nb > line1nb) {
                        var line2 = jQuery(this);

                        var line2Name = null;
                        if (useName) {
                            line2Name = line2.find('.zhyweb_comment_contest_alias').text();
                        }

                        var line2Email = null;
                        if (useEmail) {
                            line2Email = line2.find('.zhyweb_comment_contest_email').text();
                        }

                        var line2IP = null;
                        if (useIP) {
                            line2IP = line2.find('.zhyweb_comment_contest_ip').text();
                        }

                        if (line1Name === line2Name && line1Email === line2Email && line1IP === line2IP) {
                            // Both lines are the same
                            if (testFunction.call(null, line1, line2)) {
                                if (selectDuplicates) {
                                    line1.find('input[id^="cb-select"]').each(function() {
                                        var input = jQuery(this).val();
                                        if (jQuery.inArray(input, duplicates) === -1) {
                                            duplicates.push(input);
                                        }
                                    });
                                }
                                
                                line2.find('input[id^="cb-select"]').each(function() {
                                    var input = jQuery(this).val();
                                    if (jQuery.inArray(input, duplicates) === -1) {
                                        duplicates.push(input);
                                    }
                                });
                            }
                        }
                    }
                    line2nb++;
                });
            }
            line1nb++;
        });
        
        // Sort numbers in case of they are not
        duplicates.sort(function(a, b) {
            return a - b;
        });
    
        for (var i = 0; i < duplicates.length; i++) {
            var inputID = duplicates[i];
            if (i >= nbAuthEntries) {
                jQuery('#contestForm #cb-select-' + inputID).attr("checked", true);
            }
        }
    }
    
    // ------------------------ Filter action ----------------------------------
    
    function addFilterAction(actionID) {
        var useName = ('alias' === actionID);
        var useEmail = ('email' === actionID);
        var useIP = ('ip' === actionID);
        
        jQuery('#' + actionID + 'AddressFilter').click(function() {
            // Clean
            jQuery('#zwpcc_' + actionID + 'Filter_error_message').hide();
            removeCSSErrorElementID(actionID + 'Config');

            // Check input format and launch the process if it's ok
            var inputValue = jQuery('#' + actionID + 'Config').val();
            if (jQuery.isNumeric(inputValue) && inputValue >= 0 && Math.floor(inputValue) == inputValue) {
                checkInputsOfWrongComments(function(line1, line2) {
                    var data1 = line1.find('.zhyweb_comment_contest_' + actionID).text();
                    var data2 = line2.find('.zhyweb_comment_contest_' + actionID).text();
                    return data1 === data2;
                }, useName, useEmail, useIP, true, inputValue);
            } else {
                jQuery('#zwpcc_' + actionID + 'Filter_error_message').show();
                addCSSErrorElementID(actionID + 'Config');
            }
        });
    }
    
    addFilterAction("ip");
    addFilterAction("email");
    addFilterAction("alias");
    
    // ------------------------ end Filter action ------------------------------
    
    // ------------------------ Time Between Two Comments action ---------------
    
    jQuery('#timeBetweenFilter').click(function() {
        // Clean
        jQuery('#zwpcc_timeBetweenFilter_error_message').hide();
        removeCSSErrorElementID('timeBetween');

        // Check input format and launch the process if it's ok
        var inputValue = jQuery('#timeBetween').val();
        if (jQuery.isNumeric(inputValue) && inputValue > 0 && Math.floor(inputValue) == inputValue) {
            var useName = jQuery('#timeBetweenFilterName').is(':checked');
            var useEmail = jQuery('#timeBetweenFilterEmail').is(':checked');
            var useIP = jQuery('#timeBetweenFilterIP').is(':checked');
            
            if (useName || useEmail || useIP) {
                checkInputsOfWrongComments(function(line1, line2) {
                    var time1 = line1.find('.zhyweb_comment_contest_timestamp').text();
                    var time2 = line2.find('.zhyweb_comment_contest_timestamp').text();
                    var diff = (new Date(time1 * 1000).getTime() - new Date(time2 * 1000).getTime()) / 1000 / 60;
                    var testTime = Math.abs(Math.round(diff));
                    return testTime < inputValue;
                }, useName, useEmail, useIP, false, 0);
            } else {
                jQuery('#zwpcc_timeBetweenFilter_error_message').show();
            }
        } else {
            jQuery('#zwpcc_timeBetweenFilter_error_message').show();
            addCSSErrorElementID('timeBetween');
        }
    });
    
    // ------------------------ end Time Between Two Comments action -----------
    
    // ------------------------ Date filter ------------------------------------
    
    jQuery('#datepicker').datepicker();
    jQuery('#datepicker').datepicker("option", "dateFormat", "mm/dd/yy");
    
    jQuery('#dateSubmit').click(function() {
        // Clear error messages
        jQuery('#zwpcc_dateFilter_error_message').hide();
        removeCSSErrorElementID("datepicker");
        removeCSSErrorElementID("dateHours");
        removeCSSErrorElementID("dateMinutes");
        
        // Check date format
        var dateFormatOk = false;
        var dateRegex = /^(\d{1,2})\/(\d{1,2})\/(\d{4})$/;
        var dateValue = jQuery('#datepicker').val();
        var month = "";
        var day = "";
        var year = "";
        if (dateRegex.test(dateValue)) {
            var match = dateRegex.exec(dateValue);
            month = match[1];
            day = match[2];
            year = match[3];
            if (month > 0 && month < 13 && day > 0 && day <32 && year > 0) {
                dateFormatOk = true;
            }
        }
        
        if (dateFormatOk) {
            // Check hours format
            var dateHours = jQuery('#dateHours').val();
            var dateMinutes = jQuery('#dateMinutes').val();
            if (dateHours !== "" && dateHours >= 0 && dateHours < 24 && dateMinutes !== "" && dateMinutes >= 0 && dateMinutes < 60
            		&& jQuery.isNumeric(dateHours) && Math.floor(dateHours) == dateHours
            		&& jQuery.isNumeric(dateMinutes) && Math.floor(dateMinutes) == dateMinutes) {
                // Date OK => Launch selection
                jQuery('#contestForm tr').each(function() {
                    // Browse all table lines
                    var line = jQuery(this);

                    // Search for span tag with class "zhyweb_comment_contest_date"
                    var timestampComment = line.find('.zhyweb_comment_contest_date').text();
                    if (timestampComment !== "") {
                        var wantedTimestamp = year + month + day + dateHours + dateMinutes;
                        if (timestampComment > wantedTimestamp) {
                            // If comment time > deadline => Check the box in lines
                            line.find('input[id^="cb-select"]').each(function() {
                                jQuery(this).attr("checked", true);
                            });
                        }
                    }
                });
            } else {
                jQuery('#zwpcc_dateFilter_error_message').show();
                addCSSErrorElementID("dateHours");
                addCSSErrorElementID("dateMinutes");
            }
        } else {
            jQuery('#zwpcc_dateFilter_error_message').show();
            addCSSErrorElementID("datepicker");
        }
    });
    
    // ------------------------ END Date filter --------------------------------
    
    // ------------------------ Words filter -----------------------------------
    
    function selectLinesWithoutWords(all) {
        var wordsStr = jQuery('#words').val();
        var wordsArray = wordsStr.split(',');
        
        if (wordsArray.length > 0) { // Optimization test
            for (var i = 0; i < wordsArray.length; i++) {
                wordsArray[i] = wordsArray[i].trim().toLowerCase();
            }
            jQuery('#contestForm tr').each(function() {
                var comment = jQuery(this).find('.comment p').html();
                var found = all;
                for (var i = 0; i < wordsArray.length && comment != null; i++) {
                    var wordToFind = wordsArray[i];
                    var wordIndex = comment.toLowerCase().indexOf(wordToFind);
                    if (all) {
                        if (wordIndex === -1) {
                            // At least one word not found => check the input box
                            found = false;
                            break;
                        }
                    } else {
                        if (wordIndex >= 0) {
                            // At least one word found => do not check the input box
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    jQuery(this).find('input[id^="cb-select"]').attr("checked", true);
                }
            });
        }
    }
    
    jQuery('#wordsFilter').click(function() {
        selectLinesWithoutWords(false);
    });
    
    jQuery('#allWordsFilter').click(function() {
        selectLinesWithoutWords(true);
    });

    // ------------------------ end Words filter -------------------------------
    
    // ------------------------ Tooltips (Help) --------------------------------

    // Code from BackWPup - WordPress Backup Plugin
    // http://marketpress.com/product/backwpup-pro/
    
    jQuery('.help').tipTip({
            'attribute': 'title',
            'fadeIn': 50,
            'fadeOut': 50,
            'keepAlive': true,
            'activation': 'hover',
            'maxWidth': 400
    });

    jQuery(".help").tipTip();
    
    // ------------------------ END Tooltips (Help) ----------------------------


    // ------------------------ DELETE COMMENTS FROM CONTEST -------------------
    /**
     * Remove all checked comments
     */
    function deleteSelectedComments() {
        jQuery('#contestForm input[id^="cb-select"]:checked').each(function(index, domElem) {
            if (domElem.id.indexOf("-all-") === -1) {
                commentContestDelete(jQuery(this).val());
            } else {
                jQuery(this).attr("checked", false);
            }
        });
    }
    
    jQuery("#contestForm #doaction").click(function() {
        if (jQuery('#contestForm select[name="action"]').val() === 'delete') {
            deleteSelectedComments();
        }
    });
    
    jQuery("#contestForm #doaction2").click(function() {
        if (jQuery('#contestForm select[name="action2"]').val() === 'delete') {
            deleteSelectedComments();
        }
    });
    // ------------------------ end DELETE COMMENTS FROM CONTEST ---------------
    
    
    // ------------------------ RESTORE COMMENTS FOR CONTEST -------------------
    /**
     * Restore all checked comments
     */
    function restoreSelectedComments() {
        jQuery('#contestForm input[id^="cb-select"]:checked').each(function(index, domElem) {
            if (domElem.id.indexOf("-all-") === -1) {
                commentContestRestore(jQuery(this).val());
            } else {
                jQuery(this).attr("checked", false);
            }
        });
    }
    
    jQuery("#contestForm #doaction").click(function() {
        if (jQuery('#contestForm select[name="action"]').val() === 'restore') {
            restoreSelectedComments();
        }
    });
    
    jQuery("#contestForm #doaction2").click(function() {
        if (jQuery('#contestForm select[name="action2"]').val() === 'restore') {
            restoreSelectedComments();
        }
    });
    // ------------------------ end RESTORE COMMENTS FOR CONTEST ---------------
    
    
    // ------------------------ RESULT TABLE -----------------------------------
    jQuery("#dialog-modal-winners").dialog({
        height: 500,
        width: 800,
        modal: true,
        autoOpen: false,
        dialogClass: 'wp-dialog',
        open: function() {
            // While opening the result dialog...
            var nbWinners = jQuery('#zwpcc_nb_winners').val();
            var commentsNormal = new Array();
            var commentsCheat = new Array();
            
            // Get all comments ID which are used for the contest
            jQuery('#contestForm tr').each(function() {
                var line = jQuery(this);
                
                // Get only normal and cheating lines
                if (!line.hasClass("removedComment")) {
                    var commentID = line.find('.zhyweb_comment_contest_id').html();

                    // Don't get table header and footer
                    if (commentID != null && commentID !== "") {                        
                        if (line.hasClass("cheatComment")) {
                            commentsCheat.push(commentID);
                        } else {
                            commentsNormal.push(commentID);
                        }
                    }
                }
            });
            
            // Before displaying results, reset table
            jQuery('#dialog-modal-winners tr[id^="result-comment-contest"]').each(function() {
                jQuery(this).hide();
            });
            
            // Randomize arrays
            fisherYates(commentsCheat);
            if (commentsCheat.length < nbWinners) {
                // Optimisation : randomize array only if necessary
                fisherYates(commentsNormal);
            }
            
            // Show winners
            var comments = jQuery.merge(commentsCheat, commentsNormal);
            for (var i = 0; i < nbWinners && i < comments.length; i++) {
                jQuery("#result-comment-contest-" + comments[i]).show();
                if (i >= 1) {
                    // Sort table
                    jQuery("#result-comment-contest-" + comments[i - 1]).after(jQuery("#result-comment-contest-" + comments[i]));
                }
            }
        }
    });
    
    // Launch contest Button
    jQuery("#zwpcc_form").submit(function() {
        launch = true;
        
        // Clean all inputs
        jQuery("#zwpcc_nbWinners_error_message").hide();
        jQuery(this).find('input').each(function() {
            removeCSSErrorElement(jQuery(this));
        });
        jQuery("#winners-message-ok").hide();
        jQuery("#winners-message-error").hide();
        
        // Check all values
        // "Number of winners" must be numeric
        var nbWinners = jQuery("#zwpcc_nb_winners").val();
        if (!jQuery.isNumeric(nbWinners) || nbWinners <= 0) {
            launch = false;
            addCSSErrorElementID("zwpcc_nb_winners");
            jQuery("#zwpcc_nbWinners_error_message").show();
        }
        
        // Launch contest?
        if (launch) {
            jQuery("#dialog-modal-winners").dialog("open");
        }

        return false;
    });
    // ------------------------ end RESULT TABLE -------------------------------
    
    // ------------------------ CSS Functions ----------------------------------
    
    function addCSSErrorElementID(elementID) {
        jQuery("#" + elementID).css('border', '2px solid red');
    }
    
    function removeCSSErrorElement(element) {
        element.css('border', '1px solid rgb(223,223,223)');
    }
    
    function removeCSSErrorElementID(elementID) {
        removeCSSErrorElement(jQuery("#" + elementID));
    }
    
    // ------------------------ end CSS Functions ------------------------------

    // ------------------------ Save Winners Action ----------------------------

    jQuery(".saveWinnersButton").click(function () {
        var winners = [];
        jQuery('#dialog-modal-winners table tr').each(function () {
            var line = jQuery(this);
            var commentID = line.find('.zhyweb_comment_contest_id').text();
            if (commentID && line.is(":visible")) {
                winners.push(commentID);
            }
        });
        var posting = jQuery.post("../wp-content/plugins/comment-contest/php/OrgZhyweb_WPCommentContest_SaveWinners.php",
            { winners: winners.join(","), post: jQuery("#zwpcc_postID").text()});
        posting.fail(function(data) {
            jQuery("#winners-message-ok").hide();
            jQuery("#winners-message-error").show();
            jQuery("#winners-message-error-msg").text(data);
        });
        posting.done(function(data) {
            jQuery("#winners-message-error").hide();
            jQuery("#winners-message-ok").show();
        });
    });

    // ------------------------ end Save Winners Action ------------------------
    
});