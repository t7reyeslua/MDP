MDP
===

Multi User Scheduler for Energy Efficient Smart Homes [ET4380 Multidisciplinary Project]

* Implementation of framework for energy apportioning in multi-occupant households. 
* Combines data from smartphones and smartwatches to obtain fine-grained information of occupants such as their location and activities performed. 
* Estimates per-occupant energy footprint automatically by modeling the association between the appliances and occupants in the household.
* Appliance usage detection implementation using NFC tags.

Three modules:
* 1.Mobile: Android app for detecting NFC tags, detecting indoor positioning within the house and user interface, fingerprinting, configuring different settings and reporting relevant notifications to user.
* 2.Wear: Android wear app for monitoring the smartwatch sensors and collecting their data for activity recognition.
* 3.Backend: Server side code (Google Cloud Platform) that implements the WEKA model creation and evaluation and broadcasts messages to all users.
